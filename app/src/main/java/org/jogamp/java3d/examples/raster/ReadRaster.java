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

package org.jogamp.java3d.examples.raster;

import android.os.Bundle;

import javaawt.image.BufferedImage;
import javaawt.image.VMBufferedImage;
import javaawt.imageio.VMImageIO;
import jogamp.newt.driver.android.NewtBaseActivity;

import java.util.Iterator;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.GraphicsContext3D;
import org.jogamp.java3d.ImageComponent;
import org.jogamp.java3d.ImageComponent2D;
import org.jogamp.java3d.Raster;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.utils.geometry.ColorCube;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;

public class ReadRaster extends NewtBaseActivity {

	private SimpleUniverse u = null;

	public BranchGroup createSceneGraph(BufferedImage bImage, Raster readRaster)
	{
		// Create the root of the branch graph
		BranchGroup objRoot = new BranchGroup();

		// Create a Raster shape. Add it to the root of the subgraph

		ImageComponent2D drawImageComponent = new ImageComponent2D(ImageComponent.FORMAT_RGB, bImage, true, true);

		Raster drawRaster = new Raster(new Point3f(0.0f, 0.0f, 0.0f), Raster.RASTER_COLOR, 0, 0, bImage.getWidth(), bImage.getHeight(),
				drawImageComponent, null);
		Shape3D shape = new Shape3D(drawRaster);
		drawRaster.setCapability(Raster.ALLOW_IMAGE_WRITE);
		objRoot.addChild(shape);

		// Create the transform group node and initialize it to the
		// identity.  Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at runtime.  Add it to the
		// root of the subgraph.
		TransformGroup objTrans = new TransformGroup();
		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		TransformGroup cubeScale = new TransformGroup();
		Transform3D t3d = new Transform3D();
		t3d.setTranslation(new Vector3d(-0.5, 0.5, 0.0));
		cubeScale.setTransform(t3d);

		cubeScale.addChild(objTrans);
		objRoot.addChild(cubeScale);

		// Create a simple shape leaf node, add it to the scene graph.
		objTrans.addChild(new ColorCube(0.3));

		// Create a new Behavior object that will perform the desired
		// operation on the specified transform object and add it into
		// the scene graph.
		Transform3D yAxis = new Transform3D();
		Alpha rotationAlpha = new Alpha(-1, Alpha.INCREASING_ENABLE, 0, 0, 4000, 0, 0, 0, 0, 0);
		myRotationInterpolator rotator = new myRotationInterpolator(drawRaster, readRaster, rotationAlpha, objTrans, yAxis, 0.0f,
				(float) Math.PI * 2.0f);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
		rotator.setSchedulingBounds(bounds);
		objTrans.addChild(rotator);

		// Have Java 3D perform optimizations on this scene graph.
		objRoot.compile();

		return objRoot;
	}

	// ----------------------------------------------------------------

	private Canvas3D c = null;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		javaawt.image.BufferedImage.installBufferedImageDelegate(VMBufferedImage.class);
		javaawt.imageio.ImageIO.installBufferedImageImpl(VMImageIO.class);

		SimpleShaderAppearance.setVersionES300();

		int width = 128;
		int height = 128;

		ImageComponent2D readImageComponent = new ImageComponent2D(ImageComponent.FORMAT_RGB, width, height, false, true);

		Raster readRaster = new Raster(new Point3f(0.0f, 0.0f, 0.0f), Raster.RASTER_COLOR, 0, 0, width, height, readImageComponent, null);

		c = new myCanvas3D( readRaster);

		// Create a simple scene and attach it to the virtual universe
		BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		BranchGroup scene = createSceneGraph(bImage, readRaster);
		u = new SimpleUniverse(c);

		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		u.getViewingPlatform().setNominalViewingTransform();

		u.addBranchGraph(scene);
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
			if (u != null) {
				u.cleanup();
				u = null;
			}
			super.onDestroy();
		} catch (Exception e) {
			//ignore as we are done
			e.printStackTrace();
		}
	}

	class myCanvas3D extends Canvas3D
	{
		Raster readRaster;
		GraphicsContext3D gc;

		public myCanvas3D(Raster readRaster)
		{
			super();
			this.readRaster = readRaster;
			gc = getGraphicsContext3D();
		}

		@Override
		public void postSwap()
		{
			super.postSwap();
			synchronized (readRaster)
			{
				gc.readRaster(readRaster);
			}
		}
	}

	class myRotationInterpolator extends RotationInterpolator
	{
		Point3f wPos = new Point3f(0.025f, -0.025f, 0.0f);
		Raster drawRaster;
		Raster readRaster;
		BufferedImage bImage;
		ImageComponent2D newImageComponent;

		public myRotationInterpolator(Raster drawRaster, Raster readRaster, Alpha alpha, TransformGroup target, Transform3D axisOfRotation,
				float minimumAngle, float maximumAngle)
		{
			super(alpha, target, axisOfRotation, minimumAngle, maximumAngle);
			this.drawRaster = drawRaster;
			this.readRaster = readRaster;
		}

		@Override
		public void processStimulus(Iterator<WakeupCriterion> criteria)
		{
			synchronized (readRaster)
			{
				bImage = readRaster.getImage().getImage();
			}
			newImageComponent = new ImageComponent2D(ImageComponent.FORMAT_RGB, bImage, true, true);
			drawRaster.setImage(newImageComponent);
			super.processStimulus(criteria);
		}
	}
}