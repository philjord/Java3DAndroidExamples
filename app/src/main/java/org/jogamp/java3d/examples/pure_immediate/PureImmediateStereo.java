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

package org.jogamp.java3d.examples.pure_immediate;

import android.os.Bundle;

import java.util.Map;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.GraphicsContext3D;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.utils.geometry.Cone;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Vector3f;

import jogamp.newt.driver.android.NewtBaseActivity;

/**
 * Pure immediate mode stereo example program for stereo. In pure 
 * immediate mode, the renderer must be stopped on the Canvas being 
 * rendered into. In our example, this is done immediately after the 
 * canvas is created. A separate thread is started up to do the 
 * immediate mode rendering.
 */

public class PureImmediateStereo extends NewtBaseActivity implements Runnable {
    
    private SimpleUniverse univ = null;
    private BranchGroup scene = null;

    // Set this to true when the graphics card use shared z buffer 
    // in stereo mode.
    public static String defaultSharedStereoZbuffer = Boolean.TRUE.toString();

    private boolean sharedStereoZbuffer;
    private boolean stereoSupport;    

    private GraphicsContext3D gc;
    private Shape3D leftConeBody, rightConeBody;
    private Shape3D leftConeCap, rightConeCap;
    private Transform3D cmt = new Transform3D();
    private Vector3f leftTrans, rightTrans;

    // One rotation (2*PI radians) every 6 seconds
    private Alpha rotAlpha = new Alpha(-1, 6000);
    private double angle;

    // Compute data which is common for both
    // left and right eye
    void computeSharedData() {
		// Compute angle of rotation based on alpha value
		angle = rotAlpha.value() * 2.0*Math.PI;
		cmt.rotY(angle);
    }

    // Render the geometry in right eye
    void renderLeft() {
 		cmt.setTranslation(leftTrans);
		gc.setModelTransform(cmt);

		if (sharedStereoZbuffer) {
			// Graphics card shared same z buffer in stereo mode,
			// in this case we have to explicitly clearing both
			// frame buffers.
			gc.clear();
		}
		gc.draw(leftConeBody);
		gc.draw(leftConeCap);
    }

    // Render the geometry for right eye
	void renderRight() {
		cmt.setTranslation(rightTrans);
		gc.setModelTransform(cmt);

		if (sharedStereoZbuffer) {
			// Graphics card shared same z buffer in stereo mode,
			// in this case we have to explicitly clearing both
			// frame buffers.
			gc.clear();
		}
		gc.draw(rightConeBody);
		gc.draw(rightConeCap);
    }

    //
    // Run method for our immediate mode rendering thread.
    //
    public void run() {
		// Set up Graphics context
		gc = c.getGraphicsContext3D();

		// We always need to set this for PureImmediate
		// stereo mode
			gc.setBufferOverride(true);

		Color3f lightColor = new Color3f(1, 1, 1);
		Vector3f lightDir = new Vector3f(0, 0, -1);
		DirectionalLight light = new DirectionalLight(lightColor,
								  lightDir);

		gc.addLight(light);

		SimpleShaderAppearance redApp = new SimpleShaderAppearance();
		SimpleShaderAppearance greenApp = new SimpleShaderAppearance();
		Color3f ambientColor = new Color3f(0, 0, 0);
		Color3f emissiveColor = new Color3f(0, 0, 0);
		Color3f diffuseColor =  new Color3f(1, 0, 0);
		Color3f specularColor =  new Color3f(1, 1, 1);
		redApp.setMaterial(new Material(ambientColor, emissiveColor,
						diffuseColor, specularColor, 5));
		diffuseColor =  new Color3f(0, 1, 0);

		greenApp.setMaterial(new Material(ambientColor, emissiveColor,
						  diffuseColor, specularColor, 5));

		// Set up geometry
		Cone leftCone = new Cone(0.4f, 0.6f,
					 Primitive.GENERATE_NORMALS, redApp);
		Cone rightCone = new Cone(0.4f, 0.6f,
					  Primitive.GENERATE_NORMALS, greenApp);
		leftConeBody  = leftCone.getShape(Cone.BODY);
		leftConeCap   = leftCone.getShape(Cone.CAP);

		rightConeBody = rightCone.getShape(Cone.BODY);
		rightConeCap  = rightCone.getShape(Cone.CAP);
		leftTrans = new Vector3f(-0.6f, 0, 0);
		rightTrans = new Vector3f(0.6f, 0, 0);


		while (true) {
			// compute data which is can be used
			// for both left and right eye
			computeSharedData();

			if (stereoSupport) {
			if (!sharedStereoZbuffer) {
				gc.setStereoMode(GraphicsContext3D.STEREO_BOTH);
				// This clear both left and right buffers, we
				// must set STEREO_BOTH before it. Otherwise
				// it only clear LEFT or RIGHT buffer unless
				// this is invoke twice for each buffer.
				gc.clear();
			}

			gc.setStereoMode(GraphicsContext3D.STEREO_LEFT);
			renderLeft();

			gc.setStereoMode(GraphicsContext3D.STEREO_RIGHT);
			renderRight();
			} else {
			gc.clear();
			renderLeft();
			}

			// This swap both left and right buffers so
			// there is no need to set STEREO_BOTH before it
			c.swap();

			// Be polite to other threads !
			Thread.yield();
		}
    }


    private void createUniverse() {
		// Preferred to use Stereo
		//GraphicsConfigTemplate3D gct = new GraphicsConfigTemplate3D();
    	//    gct.setStereo(GraphicsConfigTemplate3D.PREFERRED);
        //GraphicsConfiguration config =
	    //GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getBestConfiguration(gct);

        c = new Canvas3D();
		Map map = c.queryProperties();

		stereoSupport = c.getStereoAvailable();

		if (stereoSupport) {
			System.out.println("This machine support stereo, you should see a red cone on the left and green cone on the right.");
			// User can overide the above default behavior using
			// java3d property.
			String str = System.getProperty("j3d.sharedstereozbuffer",
							defaultSharedStereoZbuffer);
			sharedStereoZbuffer = (new Boolean(str)).booleanValue();
		} else {
			System.out.println("Stereo is not support, you should only see the left red cone.");
		}

		if (!c.getDoubleBufferAvailable()) {
			System.out.println("Double buffer is not support !");
		}

		// we must stop the Renderer in PureImmediate mode
		c.stopRenderer();

		// Create simple universe with view branch
		univ = new SimpleUniverse(c);

		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		univ.getViewingPlatform().setNominalViewingTransform();

		// Ensure at least 5 msec per frame (i.e., < 200Hz)
		univ.getViewer().getView().setMinimumFrameCycleTime(5);

			// Start a new thread that will continuously render
		(new Thread(this)).start();
    }

	// ----------------------------------------------------------------

	private Canvas3D c = null;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SimpleShaderAppearance.setVersionES300();

		// Create Canvas3D and SimpleUniverse; add canvas to drawing panel
		createUniverse();
		//canvas.addNotify();//drawingPanel.add(canvas, java.awt.BorderLayout.CENTER);
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
