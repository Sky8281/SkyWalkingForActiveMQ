/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.plugin.activemq.v1;

import java.lang.reflect.Method;
import javax.jms.MessageProducer;
import org.apache.activemq.ActiveMQMessageProducer;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.apache.skywalking.apm.network.trace.component.OfficialComponent;

public class SendMessageInterceptor implements InstanceMethodsAroundInterceptor{

	    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
	        MethodInterceptResult result) throws Throwable {

	        ContextCarrier contextCarrier = new ContextCarrier();
			
	        ActiveMQTextMessage message = (ActiveMQTextMessage)allArguments[1];
			MessageProducer messageProducer = (MessageProducer)allArguments[0];
			ActiveMQDestination  destination = (ActiveMQDestination) messageProducer.getDestination();
			String destinationStr = destination.toString();
			String activeMQName = destinationStr;
			
			AbstractSpan	 span = ContextManager.createExitSpan("ActiveMQProduct " + destinationStr, contextCarrier, activeMQName);
			
			span.setLayer(SpanLayer.MQ);
			span.setComponent(ComponentsDefine.ACTIVE_MQ);
	        SpanLayer.asMQ(span);
			message.setProperty("destination", destinationStr);
	      
	        CarrierItem next = contextCarrier.items();
	        while (next.hasNext()) {
	            next = next.next();
	            message.setProperty(next.getHeadKey(), next.getHeadValue());
	        }
			
     

	       // EnhancedInstance callbackInstance = (EnhancedInstance)allArguments[0];
	       // if (callbackInstance != null) {
	        //   callbackInstance.setSkyWalkingDynamicField(ContextManager.capture());
			//	}
		
	    }

	
	    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
	        Object ret) throws Throwable {
	        ContextManager.stopSpan();
	        return ret;
	    }

	     public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
	        Class<?>[] argumentsTypes, Throwable t) {
	
	        ContextManager.stopSpan();
	    }

}
