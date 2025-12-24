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

package org.jogamp.java3d.examples.background;

import android.os.Bundle;
import android.view.KeyEvent;

import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.ImageComponent2D;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.behaviors.mouse.MouseRotate;
import org.jogamp.java3d.utils.behaviors.mouse.MouseTranslate;
import org.jogamp.java3d.utils.behaviors.mouse.MouseZoom;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3f;

import javaawt.image.VMBufferedImage;
import javaawt.imageio.VMImageIO;
import jogamp.newt.driver.android.NewtBaseFragmentActivity;

public class BackgroundTexture extends NewtBaseFragmentActivity {

	private SimpleUniverse univ = null;
	private BranchGroup scene = null;
	private java.net.URL bgImage = null;
	private BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);



	public BranchGroup createSceneGraph()
	{

		// Create the root of the branch graph
		BranchGroup objRoot = new BranchGroup();

		// Create a Transformgroup to scale all objects so they
		// appear in the scene.
		TransformGroup objScale = new TransformGroup();
		Transform3D t3d = new Transform3D();
		t3d.setScale(0.4);
		objScale.setTransform(t3d);
		objRoot.addChild(objScale);

		// Create the transform group node and initialize it to the
		// identity.  Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at runtime.
		TransformGroup objTrans = new TransformGroup();
		objScale.addChild(objTrans);

		TextureLoader tex = new TextureLoader(bgImage, this);

		
		
		Background bg = new Background((ImageComponent2D) tex.getTexture().getImage(0));
		bg.setApplicationBounds(bounds);
		objTrans.addChild(bg);

		Vector3f tranlation = new Vector3f(2.0f, 0.0f, 0.0f);
		Transform3D modelTransform = new Transform3D();
		Transform3D tmpTransform = new Transform3D();
		double angleInc = Math.PI / 8.0;
		double angle = 0.0;
		int numBoxes = 16;

		float scaleX[] = { 0.1f, 0.2f, 0.2f, 0.3f, 0.2f, 0.1f, 0.2f, 0.3f, 0.1f, 0.3f, 0.2f, 0.3f, 0.1f, 0.3f, 0.2f, 0.3f };

		float scaleY[] = { 0.3f, 0.4f, 0.3f, 0.4f, 0.3f, 0.4f, 0.3f, 0.4f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.4f };

		float scaleZ[] = { 0.3f, 0.2f, 0.1f, 0.1f, 0.3f, 0.2f, 0.1f, 0.3f, 0.3f, 0.2f, 0.1f, 0.3f, 0.3f, 0.2f, 0.1f, 0.2f };

		SimpleShaderAppearance a1 = new SimpleShaderAppearance();

		Color3f eColor = new Color3f(0.0f, 0.0f, 0.0f);
		Color3f sColor = new Color3f(0.5f, 0.5f, 1.0f);
		Color3f oColor = new Color3f(0.5f, 0.5f, 0.3f);

		Material m = new Material(oColor, eColor, oColor, sColor, 100.0f);
		m.setLightingEnable(true);
		a1.setMaterial(m);

		for (int i = 0; i < numBoxes; i++, angle += angleInc)
		{
			modelTransform.rotY(angle);
			tmpTransform.set(tranlation);
			modelTransform.mul(tmpTransform);

			TransformGroup tgroup = new TransformGroup(modelTransform);
			objTrans.addChild(tgroup);

			tgroup.addChild(new Box(scaleX[i], scaleY[i], scaleZ[i], Box.GENERATE_NORMALS, a1));
		}

		// Shine it with two lights.
		Color3f lColor1 = new Color3f(0.7f, 0.7f, 0.7f);
		Color3f lColor2 = new Color3f(0.2f, 0.2f, 0.1f);
		Vector3f lDir1 = new Vector3f(-1.0f, -1.0f, -1.0f);
		Vector3f lDir2 = new Vector3f(0.0f, 0.0f, -1.0f);
		DirectionalLight lgt1 = new DirectionalLight(lColor1, lDir1);
		DirectionalLight lgt2 = new DirectionalLight(lColor2, lDir2);
		lgt1.setInfluencingBounds(bounds);
		lgt2.setInfluencingBounds(bounds);
		objScale.addChild(lgt1);
		objScale.addChild(lgt2);

		return objRoot;
	}

	private Canvas3D createUniverse()
	{
		// Get the preferred graphics configuration for the default screen
		//GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

		// Create a Canvas3D using the preferred configuration
		Canvas3D c = new Canvas3D();

		// Create simple universe with view branch
		univ = new SimpleUniverse(c);

		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		univ.getViewingPlatform().setNominalViewingTransform();

		// Ensure at least 5 msec per frame (i.e., < 200Hz)
		univ.getViewer().getView().setMinimumFrameCycleTime(5);

		TransformGroup viewTrans = univ.getViewingPlatform().getViewPlatformTransform();

		// Create the rotate behavior node
		MouseRotate behavior1 = new MouseRotate(c, viewTrans);
		scene.addChild(behavior1);
		behavior1.setSchedulingBounds(bounds);

		// Create the zoom behavior node
		MouseZoom behavior2 = new MouseZoom(c, viewTrans);
		scene.addChild(behavior2);
		behavior2.setSchedulingBounds(bounds);

		// Create the translate behavior node
		MouseTranslate behavior3 = new MouseTranslate(c, viewTrans);
		scene.addChild(behavior3);
		behavior3.setSchedulingBounds(bounds);

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

		// Create the content branch and add it to the universe
		scene = createSceneGraph();

		// Create Canvas3D and SimpleUniverse; add canvas to drawing panel
		c = createUniverse();

		// Let Java 3D perform optimizations on this scene graph.
		scene.compile();

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
