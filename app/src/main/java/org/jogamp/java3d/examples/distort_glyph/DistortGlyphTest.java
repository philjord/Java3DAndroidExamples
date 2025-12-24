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

package org.jogamp.java3d.examples.distort_glyph;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;

import javaawt.Font;
import javaawt.VMFont;
import javaawt.image.VMBufferedImage;
import javaawt.imageio.VMImageIO;
import jogamp.newt.driver.android.NewtBaseActivity;

import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.Font3D;
import org.jogamp.java3d.FontExtrusion;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.Light;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PointLight;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.TexCoordGeneration;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.behaviors.mouse.MouseRotate;
import org.jogamp.java3d.utils.behaviors.mouse.MouseTranslate;
import org.jogamp.java3d.utils.behaviors.mouse.MouseZoom;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

public class DistortGlyphTest extends NewtBaseActivity {

    private SimpleUniverse univ = null;
    private BranchGroup scene = null;

    private void setupLights(BranchGroup root) {
        // set up the BoundingSphere for all the lights
        BoundingSphere bounds = new BoundingSphere(new Point3d(), 100.0);

        // Set up the ambient light
        AmbientLight lightAmbient = new AmbientLight(new Color3f(0.37f, 0.37f, 0.37f));
        lightAmbient.setInfluencingBounds(bounds);
        root.addChild(lightAmbient);

        // Set up the directional light
        Vector3f lightDirection1 = new Vector3f(0.0f, 0.0f, -1.0f);
        DirectionalLight lightDirectional1 = new DirectionalLight(new Color3f(1.00f, 0.10f, 0.00f), lightDirection1);
        lightDirectional1.setInfluencingBounds(bounds);
        lightDirectional1.setCapability(Light.ALLOW_STATE_WRITE);
        root.addChild(lightDirectional1);

        Point3f lightPos1 = new Point3f(-4.0f, 8.0f, 16.0f);
        Point3f lightAttenuation1 = new Point3f(1.0f, 0.0f, 0.0f);
        PointLight pointLight1 = new PointLight(new Color3f(0.37f, 1.00f, 0.37f), lightPos1, lightAttenuation1);
        pointLight1.setInfluencingBounds(bounds);
        root.addChild(pointLight1);

        Point3f lightPos2 = new Point3f(-16.0f, 8.0f, 4.0f);
        Point3f lightAttenuation2 = new Point3f(1.0f, 0.0f, 0.0f);
        PointLight pointLight2 = new PointLight(new Color3f(0.37f, 0.37f, 1.00f), lightPos2, lightAttenuation2);
        pointLight2.setInfluencingBounds(bounds);
        root.addChild(pointLight2);
    }
        
    public BranchGroup createSceneGraph(Canvas3D c) {
        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        setupLights(objRoot);

        TransformGroup objTransform = new TransformGroup();
        objTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

        objRoot.addChild(objTransform);

        // setup a nice textured appearance
        SimpleShaderAppearance app = new SimpleShaderAppearance();
        Color3f objColor = new Color3f(1.0f, 0.7f, 0.8f);
        Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
        app.setMaterial(new Material(objColor, black, objColor, black, 80.0f));
        Texture txtr = new TextureLoader(getClass().getResource("/resources/images/gold.jpg"),this).getTexture();
        app.setTexture(txtr);
        TexCoordGeneration tcg = new TexCoordGeneration(TexCoordGeneration.SPHERE_MAP,TexCoordGeneration.TEXTURE_COORDINATE_2);
        app.setTexCoordGeneration(tcg);

        // use a customized FontExtrusion object to control the depth of the text
        javaawt.geom.GeneralPath gp = new javaawt.geom.GeneralPath();
        gp.moveTo(0, 0);
        gp.lineTo(.01f, .01f);
        gp.lineTo(.2f, .01f);
        gp.lineTo(.21f, 0f);
        FontExtrusion fontEx = new FontExtrusion(gp);

        // our glyph
        Font fnt = new VMFont(Typeface.DEFAULT, 1);
        // note Font3D is disabled in java3d-core-and so this goes no where
        Font3D f3d = new Font3D(fnt, .001, fontEx);
        GeometryArray geom = f3d.getGlyphGeometry('A');
        Shape3D shape = new Shape3D(geom, app);
        objTransform.addChild(shape);

// note Font3D is disabled in java3d-core-and so this goes no where
        // the DistortBehavior
 //       DistortBehavior eb = new DistortBehavior(shape, 1000, 1000);
 //       eb.setSchedulingBounds(new BoundingSphere());
 //       objTransform.addChild(eb);

        MouseRotate myMouseRotate = new MouseRotate(c);
        myMouseRotate.setTransformGroup(objTransform);
        myMouseRotate.setSchedulingBounds(new BoundingSphere());
        objRoot.addChild(myMouseRotate);

        MouseTranslate myMouseTranslate = new MouseTranslate(c);
        myMouseTranslate.setTransformGroup(objTransform);
        myMouseTranslate.setSchedulingBounds(new BoundingSphere());
        objRoot.addChild(myMouseTranslate);

        MouseZoom myMouseZoom = new MouseZoom(c);
        myMouseZoom.setTransformGroup(objTransform);
        myMouseZoom.setSchedulingBounds(new BoundingSphere());
        objRoot.addChild(myMouseZoom);

        // Let Java 3D perform optimizations on this scene graph.
        objRoot.compile();

        return objRoot;
    }
    
    private Canvas3D createUniverse() {
        
        // Create a Canvas3D using a nice configuration
        Canvas3D c = new Canvas3D();

        // Create simple universe with view branch
        univ = new SimpleUniverse(c);

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

        javaawt.image.BufferedImage.installBufferedImageDelegate(VMBufferedImage.class);
        javaawt.imageio.ImageIO.installBufferedImageImpl(VMImageIO.class);

        SimpleShaderAppearance.setVersionES300();

        // Create Canvas3D and SimpleUniverse; add canvas to drawing panel
        c = createUniverse();

        // Create the content branch and add it to the universe
        scene = createSceneGraph(c);
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
