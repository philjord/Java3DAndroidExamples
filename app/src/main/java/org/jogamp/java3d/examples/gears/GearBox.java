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

package org.jogamp.java3d.examples.gears;

import android.os.Bundle;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.AmbientLight;
 
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;

import org.jogamp.java3d.utils.behaviors.mouse.MouseRotate;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import jogamp.newt.driver.android.NewtBaseActivity;

/**
 * Simple Java 3D example program to display a spinning cube.
 */
public class GearBox extends NewtBaseActivity {

    private SimpleUniverse univ = null;
    private BranchGroup scene = null;
    private int toothCount = 48;
    
    public BranchGroup createSceneGraph() {
		Transform3D tempTransform = new Transform3D();
	
		// Create the root of the branch graph
		BranchGroup branchRoot = createBranchEnvironment();

        // Create a Transformgroup to scale all objects so they
        // appear in the scene.
        TransformGroup objScale = new TransformGroup();
        Transform3D t3d = new Transform3D();
        t3d.setScale(0.4);
        objScale.setTransform(t3d);
        branchRoot.addChild(objScale);

		// Create an Appearance.
		SimpleShaderAppearance look = new SimpleShaderAppearance();
		Color3f objColor = new Color3f(0.5f, 0.5f, 0.6f);
		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
		Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
		look.setMaterial(new Material(objColor, black,
					      objColor, white, 100.0f));
	
	
		// Create the transform group node and initialize it to the
		// identity.  Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at runtime.  Add it to the
		// root of the subgraph.
		TransformGroup gearboxTrans = new TransformGroup();
		gearboxTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		gearboxTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		objScale.addChild(gearboxTrans);
	
		// Create a bounds for the mouse behavior methods
	        BoundingSphere bounds =
	            new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
		
		// Define the shaft base information
		int shaftCount = 4;
		int secondsPerRevolution = 8000;
	
		// Create the Shaft(s)
		Shaft shafts[] = new Shaft[shaftCount];
		TransformGroup shaftTGs[] = new TransformGroup[shaftCount];
		Alpha shaftAlphas[] = new Alpha[shaftCount];
		RotationInterpolator shaftRotors[]
		    = new RotationInterpolator[shaftCount];
		Transform3D shaftAxis[] = new Transform3D[shaftCount];
	
		// Note: the following arrays we're incorporated to make changing
		// the gearbox easier.
		float shaftRatios[] = new float[shaftCount];
		shaftRatios[0] = 1.0f;
		shaftRatios[1] = 0.5f;
		shaftRatios[2] = 0.75f;
		shaftRatios[3] = 5.0f;
	
		float shaftRadius[] = new float[shaftCount];
		shaftRadius[0] = 0.2f;
		shaftRadius[1] = 0.2f;
		shaftRadius[2] = 0.2f;
		shaftRadius[3] = 0.2f;
	
		float shaftLength[] = new float[shaftCount];
		shaftLength[0] = 1.8f;
		shaftLength[1] = 0.8f;
		shaftLength[2] = 0.8f;
		shaftLength[3] = 0.8f;
	
		float shaftDirection[] = new float[shaftCount];
		shaftDirection[0] = 1.0f;
		shaftDirection[1] = -1.0f;
		shaftDirection[2] = 1.0f;
		shaftDirection[3] = -1.0f;
	
		Vector3d shaftPlacement[] = new Vector3d[shaftCount];
		shaftPlacement[0] = new Vector3d(-0.75, -0.9, 0.0);
		shaftPlacement[1] = new Vector3d(0.75, -0.9, 0.0);
		shaftPlacement[2] = new Vector3d(0.75, 0.35, 0.0);
		shaftPlacement[3] = new Vector3d(-0.75, 0.60, -0.7);
	
		// Create the shafts.
		for(int i = 0; i < shaftCount; i++) {
		    shafts[i] = new Shaft(shaftRadius[i], shaftLength[i], 25, look);
		}
	
		// Create a transform group node for placing each shaft
		for(int i = 0; i < shaftCount; i++) {
		    shaftTGs[i] = new TransformGroup();
		    gearboxTrans.addChild(shaftTGs[i]);
		    shaftTGs[i].getTransform(tempTransform);
		    tempTransform.setTranslation(shaftPlacement[i]);
		    shaftTGs[i].setTransform(tempTransform);
		    shaftTGs[i].addChild(shafts[i]);
		}
	
		// Add rotation interpolators to rotate the shaft in the appropriate
		// direction and at the appropriate rate
		for(int i = 0; i < shaftCount; i++) {
		    shaftAlphas[i] = new Alpha(-1, Alpha.INCREASING_ENABLE, 0, 0,
					      (long)(secondsPerRevolution
						     * shaftRatios[i]),
					      0, 0,
					      0, 0, 0);
		    shaftAxis[i] = new Transform3D();
		    shaftAxis[i].rotX(Math.PI/2.0);
		    shaftRotors[i]
			= new RotationInterpolator(shaftAlphas[i], shafts[i],
						   shaftAxis[i],
						   0.0f,
						   shaftDirection[i] * 
						   (float) Math.PI * 2.0f);
		    shaftRotors[i].setSchedulingBounds(bounds);
		    shaftTGs[i].addChild(shaftRotors[i]);
		}
	
		// Define the gear base information.  Again, these arrays exist to
		// make the process of changing the GearBox1 via an editor faster
		int gearCount = 5;
		float valleyToCircularPitchRatio = .15f;
		float pitchCircleRadius = 1.0f;
		float addendum = 0.05f;
		float dedendum = 0.05f;
		float gearThickness = 0.3f;
		float toothTipThickness = 0.27f;
	
		// Create an array of gears and their associated information
		SpurGear gears[] = new SpurGear[gearCount];
		TransformGroup gearTGs[] = new TransformGroup[gearCount];
	
		int gearShaft[] = new int[gearCount];
		gearShaft[0] = 0;
		gearShaft[1] = 1;
		gearShaft[2] = 2;
		gearShaft[3] = 0;
		gearShaft[4] = 3;
	
		float ratio[] = new float[gearCount];
		ratio[0] = 1.0f;
		ratio[1] = 0.5f;
		ratio[2] = 0.75f;
		ratio[3] = 0.25f;
		ratio[4] = 1.25f;
	
		Vector3d placement[] = new Vector3d[gearCount];
		placement[0] = new Vector3d(0.0, 0.0, 0.0);
		placement[1] = new Vector3d(0.0, 0.0, 0.0);
		placement[2] = new Vector3d(0.0, 0.0, 0.0);
		placement[3] = new Vector3d(0.0, 0.0, -0.7);
		placement[4] = new Vector3d(0.0, 0.0, 0.0);
	
		// Create the gears.
		for(int i = 0; i < gearCount; i++) {
		    gears[i]
			= new SpurGearThinBody(((int)((float)toothCount * ratio[i])),
					       pitchCircleRadius * ratio[i],
					       shaftRadius[0],
					       addendum, dedendum,
					       gearThickness,
					       toothTipThickness,
					       valleyToCircularPitchRatio, look);
		}
	
		// Create a transform group node for arranging the gears on a shaft
		// and attach the gear to its associated shaft
		for(int i = 0; i < gearCount; i++) {
		    gearTGs[i] = new TransformGroup();
		    gearTGs[i].getTransform(tempTransform);
		    tempTransform.rotZ((shaftDirection[gearShaft[i]] == -1.0) ?
				       gears[i].getCircularPitchAngle()/-2.0f :
				       0.0f);
		    tempTransform.setTranslation(placement[i]);
		    gearTGs[i].setTransform(tempTransform);
		    gearTGs[i].addChild(gears[i]);
		    shafts[gearShaft[i]].addChild(gearTGs[i]);
		}
	

		return branchRoot;
    }

    BranchGroup createBranchEnvironment() {
        // Create the root of the branch graph
        BranchGroup branchRoot = new BranchGroup();
         
        // Create a bounds for the background and lights
        BoundingSphere bounds =
            new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

        // Set up the background
        Color3f bgColor = new Color3f(0.05f, 0.05f, 0.5f);
        Background bgNode = new Background(bgColor);
        bgNode.setApplicationBounds(bounds);
        branchRoot.addChild(bgNode);

        // Set up the ambient light
        Color3f ambientColor = new Color3f(0.1f, 0.1f, 0.1f);
        AmbientLight ambientLightNode = new AmbientLight(ambientColor);
        ambientLightNode.setInfluencingBounds(bounds);
        branchRoot.addChild(ambientLightNode);

        // Set up the directional lights
        Color3f light1Color = new Color3f(1.0f, 1.0f, 0.9f);
        Vector3f light1Direction  = new Vector3f(1.0f, 1.0f, 1.0f);
        Color3f light2Color = new Color3f(1.0f, 1.0f, 0.9f);
        Vector3f light2Direction  = new Vector3f(-1.0f, -1.0f, -1.0f);

        DirectionalLight light1
            = new DirectionalLight(light1Color, light1Direction);
        light1.setInfluencingBounds(bounds);
        branchRoot.addChild(light1);

        DirectionalLight light2
            = new DirectionalLight(light2Color, light2Direction);
        light2.setInfluencingBounds(bounds);
        branchRoot.addChild(light2);

        return branchRoot;
    }

    private Canvas3D createUniverse() {
		// Create a Canvas3D using the preferred configuration
		Canvas3D c = new Canvas3D();
	
		// Create simple universe with view branch
		univ = new SimpleUniverse(c);

		TransformGroup viewTrans = univ.getViewingPlatform().getViewPlatformTransform();
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
		// Create the rotate behavior node
		MouseRotate behavior1 = new MouseRotate(c, viewTrans);
		scene.addChild(behavior1);
		behavior1.setSchedulingBounds(bounds);
	
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
    	int value;

		//needs an on screen picker, for now just set it to 20
		String[] args = new String[]{"20"};

    	if (args.length > 1) {
            System.out.println("Usage: java GearBox  #teeth (LCD 4)");
            System.exit(0);
        } else if (args.length == 1) {
            {
                try{
                    value = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.out.println("Illegal integer specified");
                    System.out.println("Usage: java GearBox  #teeth (LCD 4)");
                    value = 0;
                    System.exit(0);
                }
                if (value <= 0 | (value % 4) != 0) {
                    System.out.println("Integer not a positive multiple of 4");
                    System.out.println("Usage: java GearBox  #teeth (LCD 4)");
                    System.exit(0);
                }
                toothCount = value;
            }
        }

		// Create the content branch and add it to the universe
		scene = createSceneGraph();

		// Create Canvas3D and SimpleUniverse; add canvas to drawing panel
		c = createUniverse();

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
    
}
