/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package org.jogamp.java3d.examples.lod;

import android.os.Bundle;
import android.view.KeyEvent;

import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.DistanceLOD;
import org.jogamp.java3d.Switch;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnElapsedFrames;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3f;

import java.util.Iterator;

import jogamp.newt.driver.android.NewtBaseActivity;

/**
 * Simple Java 3D example program to display a spinning cube.
 */
public class LOD extends NewtBaseActivity {

    private SimpleUniverse univ = null;
    private BranchGroup scene = null;
    private TransformGroup objTrans;

    public BranchGroup createSceneGraph() {
        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        createLights(objRoot);

        // Create the transform group node and initialize it to the
        // identity.  Enable the TRANSFORM_WRITE capability so that
        // our behavior code can modify it at runtime.  Add it to the
        // root of the subgraph.
        objTrans = new TransformGroup();
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        objRoot.addChild(objTrans);

        // Create a switch to hold the different levels of detail
        Switch sw = new Switch(0);
        sw.setCapability(Switch.ALLOW_SWITCH_READ);
        sw.setCapability(Switch.ALLOW_SWITCH_WRITE);

        // Create several levels for the switch, with less detailed
        // spheres for the ones which will be used when the sphere is
        // further away
        sw.addChild(new Sphere(0.4f, Sphere.GENERATE_NORMALS, 60));
        sw.addChild(new Sphere(0.4f, Sphere.GENERATE_NORMALS, 30));
        sw.addChild(new Sphere(0.4f, Sphere.GENERATE_NORMALS, 10));
        sw.addChild(new Sphere(0.4f, Sphere.GENERATE_NORMALS, 3));

        // Add the switch to the main group
        objTrans.addChild(sw);

        BoundingSphere bounds =
            new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

        // set up the DistanceLOD behavior
        float[] distances = new float[3];
        distances[0] = 5.0f;
        distances[1] = 10.0f;
        distances[2] = 25.0f;
        DistanceLOD lod = new DistanceLOD(distances);
        lod.addSwitch(sw);
        lod.setSchedulingBounds(bounds);
        objTrans.addChild(lod);

        // lets create a fun little behaviour to send teh sphere back and forth so we can see the lod making the switch
        Behavior b = new Behavior(){
            WakeupOnElapsedFrames wakeupFrame = new WakeupOnElapsedFrames(0, true);
            long startTime = System.currentTimeMillis();
            @Override
            public void initialize() {
                // Insert wakeup condition into queue
                wakeupOn(wakeupFrame);
            }

            @Override
            public void processStimulus(Iterator<WakeupCriterion> criteria) {
                //bit of odd wobbly maths
                float newDist = (float)((Math.sin((System.currentTimeMillis() - startTime)  / 1000f) + 1) * 20);

                Transform3D t = new Transform3D();
                t.set(new Vector3f(0,0,-newDist));

                objTrans.setTransform(t);

                // Insert wakeup condition into queue
                wakeupOn(wakeupFrame);
            }
        };

        b.setSchedulingBounds(bounds);
        objTrans.addChild(b);

        // Have Java 3D perform optimizations on this scene graph.
        objRoot.compile();

	    return objRoot;
    }

    private void createLights(BranchGroup graphRoot) {

        // Create a bounds for the light source influence
        BoundingSphere bounds =
            new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

        // Set up the global, ambient light
        Color3f alColor = new Color3f(0.2f, 0.2f, 0.2f);
        AmbientLight aLgt = new AmbientLight(alColor);
        aLgt.setInfluencingBounds(bounds);
        graphRoot.addChild(aLgt);

        // Set up the directional (infinite) light source
        Color3f lColor1 = new Color3f(0.9f, 0.9f, 0.9f);
        Vector3f lDir1  = new Vector3f(1.0f, 1.0f, -1.0f);
        DirectionalLight lgt1 = new DirectionalLight(lColor1, lDir1);
        lgt1.setInfluencingBounds(bounds);
        graphRoot.addChild(lgt1);
    }

    private Canvas3D createUniverse() {
        // Create a Canvas3D using the preferred configuration
        Canvas3D c = new Canvas3D();

        // Create simple universe with view branch
        univ = new SimpleUniverse(c);

        // only add zoom mouse behavior to viewingPlatform
        ViewingPlatform viewingPlatform = univ.getViewingPlatform();

        // This will move the ViewPlatform back a bit so the
        // objects in the scene can be viewed.
        viewingPlatform.setNominalViewingTransform();

        // add orbit behavior to the ViewingPlatform, but disable rotate
        // and translate
        /*OrbitBehavior orbit = new OrbitBehavior(c,
                            OrbitBehavior.REVERSE_ZOOM |
                            OrbitBehavior.DISABLE_ROTATE |
                            OrbitBehavior.DISABLE_TRANSLATE);
        BoundingSphere bounds =
            new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
        orbit.setSchedulingBounds(bounds);
        viewingPlatform.setViewPlatformBehavior(orbit);  */

        // This will move the ViewPlatform back a bit so the
        // objects in the scene can be viewed.
        univ.getViewingPlatform().setNominalViewingTransform();

        // Ensure at least 5 msec per frame (i.e., < 200Hz)
        univ.getViewer().getView().setMinimumFrameCycleTime(5);

        return c;
    }

    // ----------------------------------------------------------------

    private Canvas3D c = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SimpleShaderAppearance.setVersionES300();

        // Create Canvas3D and SimpleUniverse; add canvas to drawing panel
        c = createUniverse();

        // Create the content branch and add it to the universe
        scene = createSceneGraph();
        univ.addBranchGraph(scene);

        // make the gl window the content of this app
        this.setContentView(this.getWindow(), c.getGLWindow());
    }

    // the 4 methods below are life cycle management to keep the app stable and well behaved
    @Override
    public void onResume() {
        c.getGLWindow().setVisible(true);
        c.startRenderer();
        super.onResume();
    }

    @Override
    public void onPause() {
        c.stopRenderer();
        c.removeNotify();
        c.getGLWindow().setVisible(false);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        c.stopRenderer();
        c.removeNotify();
        c.getGLWindow().destroy();
        try {
            if (univ != null) {
                univ.cleanup();
                univ = null;
            }
            super.onDestroy();
        } catch (Exception e) {
            //ignore as we are done
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
}
