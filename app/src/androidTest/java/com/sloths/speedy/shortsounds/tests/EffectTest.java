package com.sloths.speedy.shortsounds.tests;

import junit.framework.TestCase;
import com.sloths.speedy.shortsounds.model.Effect;
import com.sloths.speedy.shortsounds.model.EqEffect;
import com.sloths.speedy.shortsounds.model.ReverbEffect;

/**
 * Created by jbusc_000 on 5/15/2015.
 * Test for future implementation of effects.
 */
public class EffectTest extends TestCase {

    /**
     * Tests to insure the constructor is working properly.
     */
    public void testEQConstructor() {
        Effect e = new EqEffect();
        assertNotNull("Effect Constructor made a null object", e);
    }

    public void testReverbConstructor() {
        Effect e = new ReverbEffect();
        assertNotNull("Effect Constructor made a null object", e);
    }

}
