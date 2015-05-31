package com.sloths.speedy.shortsounds.test;

import junit.framework.TestCase;
import com.sloths.speedy.shortsounds.model.Effect;
import com.sloths.speedy.shortsounds.model.EqEffect;
import com.sloths.speedy.shortsounds.model.ReverbEffect;

/**
 * Test for future implementation of effects.
 * @author John Buscher
 */
public class EffectTest extends TestCase {

    ///////////////////////////////////////////////////////////////////////////
    // Tests the EQ Constructor
    ///////////////////////////////////////////////////////////////////////////
    public void testEQConstructor() {
        Effect e = new EqEffect();
        assertNotNull("Effect Constructor made a null object", e);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Tests the Reverb Constructor
    ///////////////////////////////////////////////////////////////////////////
    public void testReverbConstructor() {
        Effect e = new ReverbEffect();
        assertNotNull("Effect Constructor made a null object", e);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Tests the titles of the different effects
    ///////////////////////////////////////////////////////////////////////////
    public void testTitles() {
        Effect eq = new EqEffect();
        Effect rv = new ReverbEffect();
        assertEquals("EQ effect title wrong", eq.getTitleString(), "Equalizer");
        assertEquals("Reverb effect title wrong", rv.getTitleString(), "Reverb");
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

}
