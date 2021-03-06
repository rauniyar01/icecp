/*
 * Copyright (c) 2017 Intel Corporation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intel.icecp.node.management;

import com.intel.icecp.core.channels.ChannelProvider;
import com.intel.icecp.core.management.ModulePermissions;
import com.intel.icecp.core.management.PermissionsManager;
import com.intel.icecp.core.metadata.formats.FormatEncodingException;
import com.intel.icecp.core.metadata.formats.JsonFormat;
import com.intel.icecp.core.pipeline.Pipeline;
import com.intel.icecp.core.pipeline.exception.PipelineException;
import com.intel.icecp.node.channels.file.FileChannelProvider;
import com.intel.icecp.node.messages.PermissionsMessage;
import com.intel.icecp.node.security.SecurityConstants;
import com.intel.icecp.core.security.crypto.exception.siganture.UnsupportedSignatureAlgorithmException;
import com.intel.icecp.node.security.crypto.utils.CryptoUtils;
import com.intel.icecp.core.security.crypto.exception.hash.HashError;
import com.intel.icecp.core.security.crypto.exception.hash.HashVerificationFailedException;
import com.intel.icecp.core.security.crypto.exception.key.InvalidKeyTypeException;
import com.intel.icecp.core.security.keymanagement.exception.KeyManagerNotSupportedException;
import com.intel.icecp.node.pipeline.implementations.MessageFormattingPipeline;
import com.intel.icecp.node.security.RandomBytesGenerator;
import com.intel.icecp.node.utils.StreamUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for FilePermisssioManager in case of signed permissions files
 *
 *
 */
public class FilePermissionManagerTest {

    private static final byte[] randomBytesToHash = RandomBytesGenerator.getRandomBytes(2048);

    @Before
    public void init() throws FormatEncodingException, IOException, UnsupportedSignatureAlgorithmException, KeyManagerNotSupportedException, HashError, InvalidKeyTypeException, PipelineException {

        // Create a fake signed permissions file
        PermissionsMessage message = new PermissionsMessage();
        message.grants = new ArrayList();
        message.name = "/com/intel/module";
        message.hash = new PermissionsMessage.ModuleHash();
        message.hash.hashAlgorithm = SecurityConstants.SHA256;
        message.hash.moduleJarHash = CryptoUtils.base64Encode(CryptoUtils.hash(randomBytesToHash, message.hash.hashAlgorithm));

        message.grants = new ArrayList();
        PermissionsMessage.Grant g = new PermissionsMessage.Grant();
        g.action = "subscribe,publish";
        g.target = "ndn:/intel/node";
        g.permission = "com.intel.icecp.core.permissions.ChannelPermission2";
        message.grants.add(g);

        Pipeline pipeline = MessageFormattingPipeline.create(PermissionsMessage.class, new JsonFormat<>(PermissionsMessage.class));

        // @TODO: TO WORK WITH SIGNED MESSAGES REPLACE THE PIPELINE CREATION WITH THE LINES BELOW
//		new GenericPipelineBuilder(PermissionsMessage.class, InputStream.class)
//				.addOperation(new FormattingOperation(new JsonFormat<>(PermissionsMessage.class)))
//				.addOperation(new AsymmetricSignatureOperation("/ndn/intel/node/key", null))
//				.addOperation(new FormattingOperation(new JsonFormat<>(SignedMessage.class)))
//				.build();
        // Save the message encoded into a file
        FileOutputStream fout = new FileOutputStream(new File("perm_file.json"));
        fout.write(StreamUtils.readAll((InputStream) pipeline.execute(message)));
        fout.close();

    }

    @Ignore // TODO avoid this until it can be re-factored to not depend on file system changes
    @Test
    public void testPermissionsVerification() throws Exception {
        ChannelProvider fileChannelBuilder = new FileChannelProvider();
        PermissionsManager manager = new FilePermissionsManager(Paths.get(""), fileChannelBuilder);
        ModulePermissions permissions = manager.retrievePermissions("perm_file");

        // Permissions must be not null
        Assert.assertNotNull(permissions);

        // Verify the hash of the module bytes
        if (!CryptoUtils.compareBytes(
                CryptoUtils.hash(randomBytesToHash, permissions.getHashAlgorithm()),
                CryptoUtils.base64Decode(permissions.getModuleHash()))) {
            // @Moreno: throw an exception
            throw new HashVerificationFailedException("Invalid hash in permissions file.");
        }

        try {
            Files.delete(Paths.get("perm_file.json"));
        } catch (IOException ex) {

        }

    }

}
