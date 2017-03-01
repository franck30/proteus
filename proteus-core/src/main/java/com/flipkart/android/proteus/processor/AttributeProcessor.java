/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION
 *
 * Copyright (c) 2017 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.flipkart.android.proteus.processor;

import android.content.Context;
import android.support.annotation.Nullable;

import com.flipkart.android.proteus.DataContext;
import com.flipkart.android.proteus.FormatterManager;
import com.flipkart.android.proteus.ProteusView;
import com.flipkart.android.proteus.value.AttributeResource;
import com.flipkart.android.proteus.value.Binding;
import com.flipkart.android.proteus.value.NestedBinding;
import com.flipkart.android.proteus.value.ObjectValue;
import com.flipkart.android.proteus.value.Primitive;
import com.flipkart.android.proteus.value.Resource;
import com.flipkart.android.proteus.value.StyleResource;
import com.flipkart.android.proteus.value.Value;

/**
 * @author kirankumar
 * @author adityasharat
 */
public abstract class AttributeProcessor<V> {

    @Nullable
    private static Value staticPrecompile(Primitive value, Context context, FormatterManager manager) {
        String string = value.getAsString();
        if (Binding.isBindingValue(string)) {
            return Binding.valueOf(string, manager);
        } else if (Resource.isResource(string)) {
            return Resource.valueOf(string, null, context);
        } else if (AttributeResource.isAttributeResource(string)) {
            return AttributeResource.valueOf(string, context);
        } else if (StyleResource.isStyleResource(string)) {
            return StyleResource.valueOf(string, context);
        }
        return null;
    }

    @Nullable
    private static Value staticPrecompile(ObjectValue object, Context context, FormatterManager manager) {
        Value binding = object.get(NestedBinding.NESTED_BINDING_KEY);
        if (null != binding) {
            return NestedBinding.valueOf(binding);
        }
        return null;
    }

    public void process(V view, Value value) {
        if (value.isBinding()) {
            handleBinding(view, value.getAsBinding());
        } else if (value.isResource()) {
            handleResource(view, value.getAsResource());
        } else if (value.isAttributeResource()) {
            handleAttributeResource(view, value.getAsAttributeResource());
        } else if (value.isStyleResource()) {
            handleStyleResource(view, value.getAsStyleResource());
        } else {
            handleValue(view, value);
        }
    }

    public void handleBinding(V view, Binding value) {
        DataContext dataContext = ((ProteusView) view).getViewManager().getDataContext();
        Value resolved = evaluate(value, dataContext.getData(), dataContext.getIndex());
        handleValue(view, resolved);
    }

    public abstract void handleValue(V view, Value value);

    public abstract void handleResource(V view, Resource resource);

    public abstract void handleAttributeResource(V view, AttributeResource attribute);

    public abstract void handleStyleResource(V view, StyleResource style);

    public Value precompile(Value value, Context context, FormatterManager manager) {
        Value compiled = null;
        if (value.isPrimitive()) {
            compiled = staticPrecompile(value.getAsPrimitive(), context, manager);
        } else if (value.isObject()) {
            compiled = staticPrecompile(value.getAsObject(), context, manager);
        }
        return null != compiled ? compiled : compile(value, context);
    }

    public Value compile(@Nullable Value value, Context context) {
        return value;
    }

    protected Value evaluate(Binding binding, Value data, int index) {
        return binding.evaluate(data, index);
    }

}