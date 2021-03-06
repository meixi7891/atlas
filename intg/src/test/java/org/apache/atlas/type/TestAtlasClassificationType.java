/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.atlas.type;

import java.util.*;

import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.ModelTestUtil;
import org.apache.atlas.model.instance.AtlasClassification;
import org.apache.atlas.model.typedef.AtlasBaseTypeDef;
import org.apache.atlas.model.typedef.AtlasClassificationDef;
import org.apache.atlas.model.typedef.AtlasEntityDef;
import org.apache.atlas.type.AtlasTypeRegistry.AtlasTransientTypeRegistry;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class TestAtlasClassificationType {
    private final AtlasClassificationType classificationType;
    private final List<Object>            validValues        = new ArrayList<>();
    private final List<Object>            invalidValues      = new ArrayList<>();

    {
        classificationType = getClassificationType(ModelTestUtil.getClassificationDefWithSuperTypes());

        AtlasClassification invalidValue1 = classificationType.createDefaultValue();
        AtlasClassification invalidValue2 = classificationType.createDefaultValue();
        Map<String, Object> invalidValue3 = classificationType.createDefaultValue().getAttributes();

        // invalid value for int
        invalidValue1.setAttribute(ModelTestUtil.getDefaultAttributeName(AtlasBaseTypeDef.ATLAS_TYPE_INT), "xyz");
        // invalid value for date
        invalidValue2.setAttribute(ModelTestUtil.getDefaultAttributeName(AtlasBaseTypeDef.ATLAS_TYPE_DATE), "xyz");
        // invalid value for bigint
        invalidValue3.put(ModelTestUtil.getDefaultAttributeName(AtlasBaseTypeDef.ATLAS_TYPE_BIGINTEGER), "xyz");

        validValues.add(null);
        validValues.add(classificationType.createDefaultValue());
        validValues.add(classificationType.createDefaultValue().getAttributes()); // Map<String, Object>
        invalidValues.add(invalidValue1);
        invalidValues.add(invalidValue2);
        invalidValues.add(invalidValue3);
        invalidValues.add(new AtlasClassification());     // no values for mandatory attributes
        invalidValues.add(new HashMap<>()); // no values for mandatory attributes
        invalidValues.add(1);               // incorrect datatype
        invalidValues.add(new HashSet());   // incorrect datatype
        invalidValues.add(new ArrayList()); // incorrect datatype
        invalidValues.add(new String[] {}); // incorrect datatype
    }

    @Test
    public void testClassificationTypeDefaultValue() {
        AtlasClassification defValue = classificationType.createDefaultValue();

        assertNotNull(defValue);
        assertEquals(defValue.getTypeName(), classificationType.getTypeName());
    }

    //       EntityA     EntityB    EntityE
    //       /              \       /
    //      /               \      /
    //    EntityC           EntityF
    //    /
    //   /
    // EntityD

    //       Classify1(EntityA)     Classify6(EntityB)    Classify2     Classify9(EntityE)   Classify5(EntityA,EntityC)  Classify5(EntityC,EntityA)
    //       /                \       /              \
    //      /                  \     /               \
    //    Classify3       Classify7 **invalid**    Classify8(EntityA) **invalid**
    //    /
    //   /
    // Classify4((EntityD)

    @Test
    public void testcanApplyToEntityType() throws AtlasBaseException {
        AtlasEntityDef         entityDefA   = new AtlasEntityDef("EntityA");
        AtlasEntityDef         entityDefB   = new AtlasEntityDef("EntityB");
        AtlasEntityDef         entityDefC   = new AtlasEntityDef("EntityC", null, null, null, new HashSet<>(Arrays.asList(entityDefA.getName())));
        AtlasEntityDef         entityDefD   = new AtlasEntityDef("EntityD", null, null, null, new HashSet<>(Arrays.asList(entityDefC.getName())));
        AtlasEntityDef         entityDefE   = new AtlasEntityDef("EntityE");
        AtlasEntityDef         entityDefF  =  new AtlasEntityDef("EntityF", null, null, null, new HashSet<>(Arrays.asList(entityDefB.getName(),entityDefE.getName())));

        AtlasClassificationDef classifyDef1 = new AtlasClassificationDef("Classify1", null, null, null, null, new HashSet<>(Arrays.asList(entityDefA.getName())), null);
        AtlasClassificationDef classifyDef2 = new AtlasClassificationDef("Classify2");
        AtlasClassificationDef classifyDef3 = new AtlasClassificationDef("Classify3", null, null, null, new HashSet<>(Arrays.asList(classifyDef1.getName())), null, null);
        AtlasClassificationDef classifyDef4 = new AtlasClassificationDef("Classify4", null, null, null, new HashSet<>(Arrays.asList(classifyDef1.getName())), new HashSet<>(Arrays.asList(entityDefD.getName())), null);
        AtlasClassificationDef classifyDef5 = new AtlasClassificationDef("Classify5", null, null, null, null, new HashSet<>(Arrays.asList(entityDefA.getName(),entityDefC.getName())), null);
        AtlasClassificationDef classifyDef6 = new AtlasClassificationDef("Classify6", null, null, null, null, new HashSet<>(Arrays.asList(entityDefB.getName())), null);
        AtlasClassificationDef classifyDef7 = new AtlasClassificationDef("Classify7", null, null, null,  new HashSet<>(Arrays.asList(classifyDef1.getName(),classifyDef6.getName())),null, null);
        AtlasClassificationDef classifyDef8 = new AtlasClassificationDef("Classify8", null, null, null,  new HashSet<>(Arrays.asList(classifyDef6.getName())),new HashSet<>(Arrays.asList(entityDefA.getName())), null);
        AtlasClassificationDef classifyDef9 = new AtlasClassificationDef("Classify9", null, null, null, null, new HashSet<>(Arrays.asList(entityDefE.getName())), null);
        AtlasClassificationDef classifyDef10 = new AtlasClassificationDef("Classify10", null, null, null, null, new HashSet<>(Arrays.asList(entityDefC.getName(),entityDefA.getName())), null);

        AtlasTypeRegistry registry = ModelTestUtil.getTypesRegistry();
        AtlasTransientTypeRegistry ttr  = registry.lockTypeRegistryForUpdate();

        ttr.addType(entityDefA);
        ttr.addType(entityDefB);
        ttr.addType(entityDefC);
        ttr.addType(entityDefD);
        ttr.addType(entityDefE);
        ttr.addType(entityDefF);

        ttr.addType(classifyDef1);
        ttr.addType(classifyDef2);
        ttr.addType(classifyDef3);
        ttr.addType(classifyDef4);
        ttr.addType(classifyDef5);
        ttr.addType(classifyDef6);
        ttr.addType(classifyDef9);
        ttr.addType(classifyDef10);
        registry.releaseTypeRegistryForUpdate(ttr, true);

        // test invalid adds
        ttr      = registry.lockTypeRegistryForUpdate();
        try {
            ttr.addType(classifyDef7);
            fail("Fail disjoined parent case");
        } catch (AtlasBaseException ae) {
            registry.releaseTypeRegistryForUpdate(ttr, false);
        }
        ttr      = registry.lockTypeRegistryForUpdate();
        try {
            ttr.addType(classifyDef8);
            fail("Fail trying to add an entity type that is not in the parent");
        } catch (AtlasBaseException ae) {
            registry.releaseTypeRegistryForUpdate(ttr, false);
        }

        AtlasEntityType         entityTypeA   = registry.getEntityTypeByName(entityDefA.getName());
        AtlasEntityType         entityTypeB   = registry.getEntityTypeByName(entityDefB.getName());
        AtlasEntityType         entityTypeC   = registry.getEntityTypeByName(entityDefC.getName());
        AtlasEntityType         entityTypeD   = registry.getEntityTypeByName(entityDefD.getName());
        AtlasEntityType         entityTypeE   = registry.getEntityTypeByName(entityDefE.getName());
        AtlasEntityType         entityTypeF   = registry.getEntityTypeByName(entityDefF.getName());
        AtlasClassificationType classifyType1 = registry.getClassificationTypeByName(classifyDef1.getName());
        AtlasClassificationType classifyType2 = registry.getClassificationTypeByName(classifyDef2.getName());
        AtlasClassificationType classifyType3 = registry.getClassificationTypeByName(classifyDef3.getName());
        AtlasClassificationType classifyType4 = registry.getClassificationTypeByName(classifyDef4.getName());
        AtlasClassificationType classifyType5 = registry.getClassificationTypeByName(classifyDef5.getName());
        AtlasClassificationType classifyType6 = registry.getClassificationTypeByName(classifyDef6.getName());
        AtlasClassificationType classifyType9 = registry.getClassificationTypeByName(classifyDef9.getName());
        AtlasClassificationType classifyType10 = registry.getClassificationTypeByName(classifyDef10.getName());

        // verify restrictions on Classify1
        assertTrue(classifyType1.canApplyToEntityType(entityTypeA));  // Classify1 has EntityA as an allowed type
        assertFalse(classifyType1.canApplyToEntityType(entityTypeB)); // Classify1 neither has EntityB as an allowed type nor any of super-types of EntityB
        assertTrue(classifyType1.canApplyToEntityType(entityTypeC));  // Classify1 has EntityA as an allowed type and EntityC is a sub-type of EntityA
        assertTrue(classifyType1.canApplyToEntityType(entityTypeD));  // Classify1 has EntityA as an allowed type and EntityD is a grand-sub-type of EntityA (via EntityC)

        // verify restrictions on Classify2
        assertTrue(classifyType2.canApplyToEntityType(entityTypeA)); // EntityA is allowed in Classify2
        assertTrue(classifyType2.canApplyToEntityType(entityTypeB)); // EntityB is allowed in Classify2
        assertTrue(classifyType2.canApplyToEntityType(entityTypeC)); // EntityC is allowed in Classify2
        assertTrue(classifyType2.canApplyToEntityType(entityTypeD)); // EntityD is allowed in Classify2

        // verify restrictions on Classify3; should be same as its super-type Classify1
        assertTrue(classifyType3.canApplyToEntityType(entityTypeA));  // EntityA is allowed in Classify3, since it is allowed in Classify1
        assertFalse(classifyType3.canApplyToEntityType(entityTypeB)); // EntityB is not an allowed type in Classify3 and Classify1
        assertTrue(classifyType3.canApplyToEntityType(entityTypeC));  // EntityC is allowed in Classify3, since its super-type EntityA is allowed in Classify1
        assertTrue(classifyType3.canApplyToEntityType(entityTypeD));  // EntityD is allowed in Classify3. since its grand-super-type EntityA is allowed in Classify1

        // verify restrictions on Classify3; should be same as its super-type Classify1
        assertFalse(classifyType4.canApplyToEntityType(entityTypeA)); // EntityA is not allowed in Classify4, though it is allowed in its super-types
        assertFalse(classifyType4.canApplyToEntityType(entityTypeB)); // EntityB is not an allowed type in Classify4
        assertFalse(classifyType4.canApplyToEntityType(entityTypeC)); // EntityC is allowed in Classify4, though it is allowed in its super-types
        assertTrue(classifyType4.canApplyToEntityType(entityTypeD));  // EntityD is allowed in Classify4

        // Trying to duplicate the pattern where a classification(Classify6) is defined on Reference(EntityB) and a classification (Classify9) is defined on asset (EntityE),
        // dataset (EntityF) inherits from both entityDefs.
        assertTrue(classifyType6.canApplyToEntityType(entityTypeF)); // EntityF can be classified by Classify6
        assertTrue(classifyType9.canApplyToEntityType(entityTypeF)); // EntityF can be classified by Classify9

        // check the that listing 2 entitytypes (with inheritance relaitonship) in any order allows classification to be applied to either entitytype.
        assertTrue(classifyType5.canApplyToEntityType(entityTypeA)); // EntityA can be classified by Classify5
        assertTrue(classifyType5.canApplyToEntityType(entityTypeC)); // EntityC can be classified by Classify5
        assertTrue(classifyType10.canApplyToEntityType(entityTypeA)); // EntityA can be classified by Classify10
        assertTrue(classifyType10.canApplyToEntityType(entityTypeC)); // EntityC can be classified by Classify10


    }

    @Test
    public void testClassificationTypeIsValidValue() {
        for (Object value : validValues) {
            assertTrue(classificationType.isValidValue(value), "value=" + value);
        }

        for (Object value : invalidValues) {
            assertFalse(classificationType.isValidValue(value), "value=" + value);
        }
    }

    @Test
    public void testClassificationTypeGetNormalizedValue() {
        assertNull(classificationType.getNormalizedValue(null), "value=" + null);

        for (Object value : validValues) {
            if (value == null) {
                continue;
            }

            Object normalizedValue = classificationType.getNormalizedValue(value);

            assertNotNull(normalizedValue, "value=" + value);
        }

        for (Object value : invalidValues) {
            assertNull(classificationType.getNormalizedValue(value), "value=" + value);
        }
    }

    @Test
    public void testClassificationTypeValidateValue() {
        List<String> messages = new ArrayList<>();
        for (Object value : validValues) {
            assertTrue(classificationType.validateValue(value, "testObj", messages));
            assertEquals(messages.size(), 0, "value=" + value);
        }

        for (Object value : invalidValues) {
            assertFalse(classificationType.validateValue(value, "testObj", messages));
            assertTrue(messages.size() > 0, "value=" + value);
            messages.clear();
        }
    }

    private static AtlasClassificationType getClassificationType(AtlasClassificationDef classificationDef) {
        try {
            return new AtlasClassificationType(classificationDef, ModelTestUtil.getTypesRegistry());
        } catch (AtlasBaseException excp) {
            return null;
        }
    }
}
