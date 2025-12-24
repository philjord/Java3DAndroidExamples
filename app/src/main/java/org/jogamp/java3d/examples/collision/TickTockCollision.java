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

package org.jogamp.java3d.examples.collision;

import android.os.Bundle;
import android.view.KeyEvent;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.geometry.ColorCube;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3d;

import javaawt.image.VMBufferedImage;
import javaawt.imageio.VMImageIO;
import jogamp.newt.driver.android.NewtBaseActivity;

/**
 * Simple Java 3D example program to display how collision work.
 */
public class TickTockCollision extends NewtBaseActivity {

    private SimpleUniverse univ = null;
    private BranchGroup scene = null;

    public BranchGroup createSceneGraph() {
		// Create the root of the branch graph
		BranchGroup objRoot = new BranchGroup();

        // Create a Transformgroup to scale all objects so they
        // appear in the scene.
        TransformGroup objScale = new TransformGroup();
        Transform3D t3d = new Transform3D();
        t3d.setScale(0.4);
        objScale.setTransform(t3d);
        objRoot.addChild(objScale);

		// Create a bounds for the background and behaviors
		BoundingSphere bounds =
			new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

		// Set up the background
		Color3f bgColor = new Color3f(0.05f, 0.05f, 0.2f);
		Background bg = new Background(bgColor);
		bg.setApplicationBounds(bounds);
		objScale.addChild(bg);

		// Create a pair of transform group nodes and initialize them to
		// identity.  Enable the TRANSFORM_WRITE capability so that
		// our behaviors can modify them at runtime.  Add them to the
		// root of the subgraph.
		TransformGroup objTrans1 = new TransformGroup();
		objTrans1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objScale.addChild(objTrans1);

		TransformGroup objTrans2 = new TransformGroup();
		objTrans2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objTrans1.addChild(objTrans2);

		// Create the positioning and scaling transform group node.
		Transform3D t = new Transform3D();
		t.set(0.3, new Vector3d(0.0, -1.5, 0.0));
		TransformGroup objTrans3 = new TransformGroup(t);
		objTrans2.addChild(objTrans3);

		// Create a simple shape leaf node, add it to the scene graph.
		objTrans3.addChild(new ColorCube());

		// Create a new Behavior object that will perform the desired
		// rotation on the specified transform object and add it into
		// the scene graph.
		Transform3D yAxis1 = new Transform3D();
		yAxis1.rotX(Math.PI/2.0);
		Alpha tickTockAlpha = new Alpha(-1, Alpha.INCREASING_ENABLE |
						Alpha.DECREASING_ENABLE,
						0, 0,
						5000, 2500, 200,
						5000, 2500, 200);

		RotationInterpolator tickTock =
			new RotationInterpolator(tickTockAlpha, objTrans1, yAxis1,
						 -(float) Math.PI/2.0f,
						 (float) Math.PI/2.0f);
		tickTock.setSchedulingBounds(bounds);
		objTrans2.addChild(tickTock);

		// Create a new Behavior object that will perform the desired
		// rotation on the specified transform object and add it into
		// the scene graph.
		Transform3D yAxis2 = new Transform3D();
		Alpha rotationAlpha = new Alpha(-1, Alpha.INCREASING_ENABLE,
						0, 0,
						4000, 0, 0,
						0, 0, 0);

		RotationInterpolator rotator =
			new RotationInterpolator(rotationAlpha, objTrans2, yAxis2,
						 0.0f, (float) Math.PI*2.0f);
		rotator.setSchedulingBounds(bounds);
		objTrans2.addChild(rotator);

		// Now create a pair of rectangular boxes, each with a collision
		// detection behavior attached.  The behavior will highlight the
		// object when it is in a state of collision.

		Group box1 = createBox(0.3, new Vector3d(-1.3, 0.0, 0.0));
		Group box2 = createBox(0.3, new Vector3d( 1.3, 0.0, 0.0));

		objScale.addChild(box1);
		objScale.addChild(box2);

        // Have Java 3D perform optimizations on this scene graph.
        objRoot.compile();

		return objRoot;
    }

    private Group createBox(double scale, Vector3d pos) {
		// Create a transform group node to scale and position the object.
		Transform3D t = new Transform3D();
		t.set(scale, pos);
		TransformGroup objTrans = new TransformGroup(t);

		// Create a simple shape leaf node and add it to the scene graph
		Shape3D shape = new Box(0.5, 5.0, 1.0);
		objTrans.addChild(shape);

		// Create a new ColoringAttributes object for the shape's
		// appearance and make it writable at runtime.
		Appearance app = shape.getAppearance();
		((SimpleShaderAppearance)app).setUpdatableCapabilities();
		ColoringAttributes ca = new ColoringAttributes();
		ca.setColor(0.6f, 0.3f, 0.0f);
		app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
		app.setColoringAttributes(ca);

		// Create a new Behavior object that will perform the collision
		// detection on the specified object, and add it into
		// the scene graph.
		CollisionDetector cd = new CollisionDetector(shape);
		BoundingSphere bounds =
			new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
		cd.setSchedulingBounds(bounds);

		// Add the behavior to the scene graph
		objTrans.addChild(cd);

		return objTrans;
    }

    private Canvas3D createUniverse() {
		// Create a Canvas3D using the preferred configuration
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
