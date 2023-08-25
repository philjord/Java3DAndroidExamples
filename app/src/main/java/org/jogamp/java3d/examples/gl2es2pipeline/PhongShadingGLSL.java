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

package org.jogamp.java3d.examples.gl2es2pipeline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jogamp.newt.opengl.GLWindow;

import java.io.IOException;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.GLSLShaderProgram;
import org.jogamp.java3d.Light;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PointLight;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.Shader;
import org.jogamp.java3d.ShaderAppearance;
import org.jogamp.java3d.ShaderError;
import org.jogamp.java3d.ShaderErrorListener;
import org.jogamp.java3d.ShaderProgram;
import org.jogamp.java3d.SourceCodeShader;
import org.jogamp.java3d.SpotLight;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.examples.alternate_appearance.AlternateAppearanceBoundsTest;
import org.jogamp.java3d.examples.java3dexamples.R;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.shader.StringIO;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import javaawt.image.VMBufferedImage;
import javaawt.imageio.VMImageIO;
import jogamp.newt.driver.android.NewtBaseFragment;
import jogamp.newt.driver.android.NewtBaseFragmentActivity;

/**
 *
 * @author  kcr
 */
public class PhongShadingGLSL extends NewtBaseFragmentActivity {

	// Constants for type of light to use
	private static final int DIRECTIONAL_LIGHT = 0;
	private static final int POINT_LIGHT = 1;
	private static final int SPOT_LIGHT = 2;

	// Flag indicates type of lights: directional, point, or spot lights.
	private static int lightType = DIRECTIONAL_LIGHT;

	private SimpleUniverse univ = null;

	private ShaderAppearance sApp = null;
	private ShaderProgram gouraudSP = null;
	private ShaderProgram phongSP = null;

	public BranchGroup createSceneGraph()
	{
		Color3f eColor = new Color3f(0.0f, 0.0f, 0.0f);
		Color3f sColor = new Color3f(1.0f, 1.0f, 1.0f);
		Color3f objColor = new Color3f(0.6f, 0.6f, 0.6f);
		//        Color3f lColor1   = new Color3f(1.0f, 0.0f, 0.0f);
		//        Color3f lColor2   = new Color3f(0.0f, 1.0f, 0.0f);
		Color3f lColor1 = new Color3f(1.0f, 1.0f, 0.5f);
		Color3f alColor = new Color3f(0.2f, 0.2f, 0.2f);
		Color3f bgColor = new Color3f(0.05f, 0.05f, 0.2f);

		Transform3D t;

		// Create the root of the branch graph
		BranchGroup objRoot = new BranchGroup();

		// Create a Transformgroup to scale all objects so they
		// appear in the scene.
		TransformGroup objScale = new TransformGroup();
		Transform3D t3d = new Transform3D();
		t3d.setScale(0.5);
		objScale.setTransform(t3d);
		objRoot.addChild(objScale);

		// Create a bounds for the background and lights
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

		// Set up the background
		Background bg = new Background(bgColor);
		bg.setApplicationBounds(bounds);
		objRoot.addChild(bg);

		// Create the TransformGroup node and initialize it to the
		// identity. Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at run time. Add it to
		// the root of the subgraph.
		TransformGroup objTrans = new TransformGroup();
		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objScale.addChild(objTrans);

		// Create a Sphere object, generate one copy of the sphere,
		// and add it into the scene graph.
		sApp = new ShaderAppearance();
		sApp.setCapability(ShaderAppearance.ALLOW_SHADER_PROGRAM_WRITE);
		Material m = new Material(objColor, eColor, objColor, sColor, 100.0f);
		sApp.setMaterial(m);

		// Create Gouraud and Phong shader programs
		String vertexProgram = null;
		String fragmentProgram = null;
		Shader[] shaders = new Shader[2];
		//String[] attrNames = { "numLights" };

		try
		{
			vertexProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/gouraud.vert"));
			fragmentProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/gouraud.frag"));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		shaders[0] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_VERTEX, vertexProgram);
		shaders[1] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_FRAGMENT, fragmentProgram);
		gouraudSP = new GLSLShaderProgram();
		gouraudSP.setShaders(shaders);

		try
		{
			vertexProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/phong.vert"));
			fragmentProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/phong.frag"));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		shaders[0] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_VERTEX, vertexProgram);
		shaders[1] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_FRAGMENT, fragmentProgram);
		phongSP = new GLSLShaderProgram();
		phongSP.setShaders(shaders);

		//if (gouraudButton.isSelected())
		{
			sApp.setShaderProgram(gouraudSP);
		}
		//else if (phongButton.isSelected())
		{
		//	sApp.setShaderProgram(phongSP);
		}
		Sphere sph = new Sphere(1.0f, Sphere.GENERATE_NORMALS, 30, sApp);
		SphereGLSL.makeNIO(sph);
		objTrans.addChild(sph);

		// Create a new Behavior object that will perform the
		// desired operation on the specified transform and add
		// it into the scene graph.
		Transform3D yAxis = new Transform3D();
		yAxis.rotZ(Math.PI);
		Alpha rotationAlpha = new Alpha(-1, 10000);

		RotationInterpolator rotator = new RotationInterpolator(rotationAlpha, objTrans, yAxis, 0.0f, (float) Math.PI * 2.0f);
		rotator.setSchedulingBounds(bounds);
		objRoot.addChild(rotator);

		// Create the transform group node for the each light and initialize
		// it to the identity.  Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at runtime.  Add them to the root
		// of the subgraph.
		TransformGroup l1RotTrans = new TransformGroup();
		l1RotTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objScale.addChild(l1RotTrans);

		TransformGroup l2RotTrans = new TransformGroup();
		l2RotTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objScale.addChild(l2RotTrans);

		// Create transformations for the positional lights
		t = new Transform3D();
		Vector3d lPos1 = new Vector3d(0.0, 0.0, 2.0);
		t.set(lPos1);
		TransformGroup l1Trans = new TransformGroup(t);
		l1RotTrans.addChild(l1Trans);

		//        t = new Transform3D();
		//        Vector3d lPos2 = new Vector3d(0.5, 0.8, 2.0);
		//        t.set(lPos2);
		//        TransformGroup l2Trans = new TransformGroup(t);
		//        l2RotTrans.addChild(l2Trans);

		// Create Geometry for point lights
		ColoringAttributes caL1 = new ColoringAttributes();
		//        ColoringAttributes caL2 = new ColoringAttributes();
		caL1.setColor(lColor1);
		//        caL2.setColor(lColor2);
		SimpleShaderAppearance appL1 = new SimpleShaderAppearance();
		//        Appearance appL2 = new Appearance();
		appL1.setColoringAttributes(caL1);
		//        appL2.setColoringAttributes(caL2);

		Sphere sph2 = new Sphere(0.05f, appL1);
		SphereGLSL.makeNIO(sph2);
		l1Trans.addChild(sph2);
		//        l2Trans.addChild(new Sphere(0.05f, appL2));

		// Create lights
		AmbientLight aLgt = new AmbientLight(alColor);

		Light lgt1 = null;
		//        Light lgt2 = null;

		Point3f lPoint = new Point3f(0.0f, 0.0f, 0.0f);
		Point3f atten = new Point3f(1.0f, 0.0f, 0.0f);
		Vector3f lDirect1 = new Vector3f(lPos1);
		//        Vector3f lDirect2 = new Vector3f(lPos2);
		lDirect1.negate();
		//        lDirect2.negate();

		switch (lightType)
		{
		case DIRECTIONAL_LIGHT:
			lgt1 = new DirectionalLight(lColor1, lDirect1);
			//            lgt2 = new DirectionalLight(lColor2, lDirect2);
			break;
		case POINT_LIGHT:
			assert false : "can't get here";
			lgt1 = new PointLight(lColor1, lPoint, atten);
			//            lgt2 = new PointLight(lColor2, lPoint, atten);
			break;
		case SPOT_LIGHT:
			assert false : "can't get here";
			lgt1 = new SpotLight(lColor1, lPoint, atten, lDirect1, 25.0f * (float) Math.PI / 180.0f, 10.0f);
			//            lgt2 = new SpotLight(lColor2, lPoint, atten, lDirect2,
			//                                 25.0f * (float)Math.PI / 180.0f, 10.0f);
			break;
		}

		// Set the influencing bounds
		aLgt.setInfluencingBounds(bounds);
		lgt1.setInfluencingBounds(bounds);
		//        lgt2.setInfluencingBounds(bounds);

		// Add the lights into the scene graph
		objScale.addChild(aLgt);
		l1Trans.addChild(lgt1);
		//        l2Trans.addChild(lgt2);

		// Create a new Behavior object that will perform the desired
		// operation on the specified transform object and add it into the
		// scene graph.
		yAxis = new Transform3D();
		Alpha rotor1Alpha = new Alpha(-1, Alpha.INCREASING_ENABLE, 0, 0, 4000, 0, 0, 0, 0, 0);
		RotationInterpolator rotator1 = new RotationInterpolator(rotor1Alpha, l1RotTrans, yAxis, 0.0f, (float) Math.PI * 2.0f);
		rotator1.setSchedulingBounds(bounds);
		l1RotTrans.addChild(rotator1);

		// Create a new Behavior object that will perform the desired
		// operation on the specified transform object and add it into the
		// scene graph.
		Alpha rotor2Alpha = new Alpha(-1, Alpha.INCREASING_ENABLE, 0, 0, 1000, 0, 0, 0, 0, 0);
		RotationInterpolator rotator2 = new RotationInterpolator(rotor2Alpha, l2RotTrans, yAxis, 0.0f, 0.0f);
		bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
		rotator2.setSchedulingBounds(bounds);
		l2RotTrans.addChild(rotator2);

		return objRoot;
	}

	 

	private Canvas3D initScene()
	{
		Canvas3D c = new Canvas3D();

		univ = new SimpleUniverse(c);

		// Add a ShaderErrorListener
		univ.addShaderErrorListener(new ShaderErrorListener() {
			@Override
			public void errorOccurred(ShaderError error)
			{
				error.printVerbose();
				PhongShadingGLSL.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(PhongShadingGLSL.this, error.toString(), Toast.LENGTH_LONG ).show();
					}
				});
			}
		});

		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		univ.getViewingPlatform().setNominalViewingTransform();

		BranchGroup scene = createSceneGraph();
		univ.addBranchGraph(scene);

		return c;
	}

	// ----------------------------------------------------------------

	private Canvas3D c = null;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phonshadingglsl);

		javaawt.image.BufferedImage.installBufferedImageDelegate(VMBufferedImage.class);
		javaawt.imageio.ImageIO.installBufferedImageImpl(VMImageIO.class);

		SimpleShaderAppearance.setVersionES300();

		// Create the scene and add the Canvas3D to the drawing panel
		c = initScene();

		// Android Fragments are a pain...
		NewtBaseFragment nbf = new AlternateAppearanceBoundsTest.NewtBaseFragment2(c.getGLWindow());
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.java3dSpace, nbf);
		fragmentTransaction.commit();
	}
	// create a NewtBaseFragment Fragment and put it into the frame layout waiting for it
	public static class NewtBaseFragment2 extends NewtBaseFragment
	{
		private GLWindow gl_window;
		public NewtBaseFragment2(GLWindow gl_window) {
			this.gl_window = gl_window;
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			// when embedding glwindows in a framelayout we need to change the defaults to something that doesn't require a request
			gl_window.setFullscreen(false);
			gl_window.setUndecorated(false);
			View rootView = getContentView(this.getWindow(), gl_window);
			return rootView;
		}
	};

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

	public void phongButtonActionPerformed(View view)
	{
		sApp.setShaderProgram(phongSP);
	}

	public void gouraudButtonActionPerformed(View view)
	{
		sApp.setShaderProgram(gouraudSP);
	}

}
