/*
 * Copyright (c) 2016 JogAmp Community. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the JogAmp Community.
 *
 */

package org.jogamp.java3d.examples.raster;

import android.os.Bundle;
import android.view.KeyEvent;

import java.net.URL;

import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.ImageComponent2D;
import org.jogamp.java3d.Raster;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.behaviors.mouse.MouseRotate;
import org.jogamp.java3d.utils.behaviors.mouse.MouseTranslate;
import org.jogamp.java3d.utils.behaviors.mouse.MouseZoom;
import org.jogamp.java3d.utils.geometry.ColorCube;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;

import javaawt.image.VMBufferedImage;
import javaawt.imageio.VMImageIO;
import jogamp.newt.driver.android.NewtBaseActivity;

/**
 * OffScreenTest programs with no UI.
 */
public class RasterTest extends NewtBaseActivity {

	private SimpleUniverse univ = null;
	private BranchGroup scene = null;
	private Raster drawRaster = null;
	private BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

	private BranchGroup createSceneGraph()
	{
		// Create the root of the branch graph
		BranchGroup objRoot = new BranchGroup();

		// trans object has composited transformation matrix
		Transform3D trans = new Transform3D();
		Transform3D rot = new Transform3D();

		trans.rotX(Math.PI / 4.0d);
		rot.rotY(Math.PI / 5.0d);
		trans.mul(rot);
		trans.setScale(0.7);
		trans.setTranslation(new Vector3d(-0.4, 0.3, 0.0));

		TransformGroup objTrans = new TransformGroup(trans);
		objRoot.addChild(objTrans);

		// Create a simple shape leaf node, add it to the scene graph.
		// ColorCube is a Convenience Utility class
		objTrans.addChild(new ColorCube(0.4));

		//Create a raster 
		URL bgImage = null;
		if (bgImage == null)
		{
			// the path to the image for an applet
			bgImage = getClass().getResource("/resources/images/bg.jpg");
			if (bgImage == null)
			{
				System.err.println("/resources/images/bg.jpg not found");
				System.exit(1);
			}
		}

		TextureLoader tex = new TextureLoader(bgImage, true);

		ImageComponent2D buffer2 = (ImageComponent2D) tex.getTexture().getImage(0);
		buffer2.setCapability(ImageComponent2D.ALLOW_IMAGE_READ);
		drawRaster = new Raster(new Point3f(0.0f, 0.0f, 0.0f), Raster.RASTER_COLOR, 0, 0, 200, 200, buffer2, null);

		drawRaster.setCapability(Raster.ALLOW_IMAGE_WRITE);
		Shape3D shape = new Shape3D(drawRaster);
		objRoot.addChild(shape);

		return objRoot;
	}

	private Canvas3D createCanvas3DAndUniverse()
	{
		// Create a Canvas3D using the preferred configuration
		Canvas3D canvas3D = new Canvas3D(false);

		// Create simple universe with view branch
		univ = new SimpleUniverse(canvas3D);

		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		univ.getViewingPlatform().setNominalViewingTransform();

		// Ensure at least 5 msec per frame (i.e., < 200Hz)
		univ.getViewer().getView().setMinimumFrameCycleTime(5);

		TransformGroup viewTrans = univ.getViewingPlatform().getViewPlatformTransform();

		// Create the rotate behavior node
		MouseRotate behavior1 = new MouseRotate(canvas3D, viewTrans);
		scene.addChild(behavior1);
		behavior1.setSchedulingBounds(bounds);

		// Create the zoom behavior node
		MouseZoom behavior2 = new MouseZoom(canvas3D, viewTrans);
		scene.addChild(behavior2);
		behavior2.setSchedulingBounds(bounds);

		// Create the translate behavior node
		MouseTranslate behavior3 = new MouseTranslate(canvas3D, viewTrans);
		scene.addChild(behavior3);
		behavior3.setSchedulingBounds(bounds);

		return canvas3D;
	}

	// ----------------------------------------------------------------

	private Canvas3D c = null;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		javaawt.image.BufferedImage.installBufferedImageDelegate(VMBufferedImage.class);
		javaawt.imageio.ImageIO.installBufferedImageImpl(VMImageIO.class);

		SimpleShaderAppearance.setVersionES300();

		// Create the content branch and add it to the universe
		scene = createSceneGraph();

		// Create an canvas3D3D and SimpleUniverse; add canvas to drawing panel
		c = createCanvas3DAndUniverse();

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
