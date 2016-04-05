/*
 * Copyright 1999-2101 Alibaba Group.
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
package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Member;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.util.FieldInfo;

/**
 * @author wenshao[szujobs@hotmail.com]
 */
public abstract class FieldSerializer implements Comparable<FieldSerializer> {

    public final FieldInfo fieldInfo;
    private String         single_quoted_fieldPrefix;
    private String         un_quoted_fieldPrefix;
    protected boolean      writeNull = false;
    
    protected int          features;

    public FieldSerializer(FieldInfo fieldInfo){
        super();
        this.fieldInfo = fieldInfo;

        JSONField annotation = fieldInfo.getAnnotation();
        if (annotation != null) {
            for (SerializerFeature feature : annotation.serialzeFeatures()) {
                if (feature == SerializerFeature.WriteMapNullValue) {
                    writeNull = true;
                }
            }
        }
    }

    public void writePrefix(JSONSerializer serializer) throws IOException {
        SerializeWriter out = serializer.out;
        
        final int featurs = out.features;
        
        if ((featurs & SerializerFeature.QuoteFieldNames.mask) != 0) {
            if ((featurs & SerializerFeature.UseSingleQuotes.mask) != 0) {
                if (single_quoted_fieldPrefix == null) {
                    single_quoted_fieldPrefix = '\'' + fieldInfo.name + "\':";
                }
                out.write(single_quoted_fieldPrefix);
            } else {
                out.write(fieldInfo.name_chars, 0, fieldInfo.name_chars.length);
            }
        } else {
            if (un_quoted_fieldPrefix == null) {
                un_quoted_fieldPrefix  = fieldInfo.name + ":";
            }
            out.write(un_quoted_fieldPrefix);
        }
    }

    public Object getPropertyValue(Object object) throws Exception {
        try {
            return fieldInfo.get(object);
        } catch (Exception ex) {
            
            Member member;
            
            if (fieldInfo.method != null) {
                member = fieldInfo.method;
            } else {
                member = fieldInfo.field;
            }
            
            String qualifiedName = member.getDeclaringClass().getName() + "." + member.getName();
            
            throw new JSONException("get property error。 " + qualifiedName, ex);
        }
    }

    public abstract void writeValue(JSONSerializer serializer, Object propertyValue) throws Exception;
    
    public int compareTo(FieldSerializer o) {
        return this.fieldInfo.compareTo(o.fieldInfo);
    }
}
