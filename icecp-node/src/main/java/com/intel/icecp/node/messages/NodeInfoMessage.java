/*
 * ******************************************************************************
 *
 * INTEL CONFIDENTIAL
 *
 * Copyright 2013 - 2016 Intel Corporation All Rights Reserved.
 *
 * The source code contained or described herein and all documents related to
 * the source code ("Material") are owned by Intel Corporation or its suppliers
 * or licensors. Title to the Material remains with Intel Corporation or its
 * suppliers and licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and licensors. The
 * Material is protected by worldwide copyright and trade secret laws and treaty
 * provisions. No part of the Material may be used, copied, reproduced,
 * modified, published, uploaded, posted, transmitted, distributed, or disclosed
 * in any way without Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other intellectual
 * property right is granted to or conferred upon you by disclosure or delivery
 * of the Materials, either expressly, by implication, inducement, estoppel or
 * otherwise. Any license under such intellectual property rights must be
 * express and approved by Intel in writing.
 *
 * Unless otherwise agreed by Intel in writing, you may not remove or alter this
 * notice or any other notice embedded in Materials by Intel or Intel's
 * suppliers or licensors in any way.
 *
 * ******************************************************************************
 */
package com.intel.icecp.node.messages;

import com.intel.icecp.core.Message;
import com.intel.icecp.node.utils.VersionUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent the node's detailed information.
 *
 */
public class NodeInfoMessage implements Message {

    public final String name;
    public final String uri;
    public final String[] features;
    public final String[] channels;
    public final List<FaceConfigurationMessage> mesh = new ArrayList<>();
    public final String version = VersionUtils.retrieveCurrentVersion();

    public NodeInfoMessage(String name, String[] featureNames, String[] channelNames, String uri) {
        this.name = name;
        this.features = featureNames;
        this.channels = channelNames;
        this.uri = uri;
    }
}