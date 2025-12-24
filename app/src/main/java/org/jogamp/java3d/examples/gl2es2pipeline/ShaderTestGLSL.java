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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jogamp.newt.opengl.GLWindow;

import java.io.IOException;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.GLSLShaderProgram;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PositionInterpolator;
import org.jogamp.java3d.Shader;
import org.jogamp.java3d.ShaderAppearance;
import org.jogamp.java3d.ShaderAttributeObject;
import org.jogamp.java3d.ShaderAttributeSet;
import org.jogamp.java3d.ShaderAttributeValue;
import org.jogamp.java3d.ShaderError;
import org.jogamp.java3d.ShaderErrorListener;
import org.jogamp.java3d.ShaderProgram;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.SourceCodeShader;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.examples.alternate_appearance.AlternateAppearanceBoundsTest;
import org.jogamp.java3d.examples.java3dexamples.R;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.shader.StringIO;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;

import jogamp.newt.driver.android.NewtBaseFragment;
import jogamp.newt.driver.android.NewtBaseFragmentActivity;

public class ShaderTestGLSL extends NewtBaseFragmentActivity {

	static final int GOLD = 1;
	static final int SILVER = 2;

	static final int DIMPLE_SHADER = 1;
	static final int BRICK_SHADER = 2;
	static final int WOOD_SHADER = 3;
	static final int POLKADOT3D_SHADER = 4;

	static final String[] shaderAttrNames1 = { "Density", "Size", "LightPosition", "Color" };

	static final String[] shaderAttrNames2 = { "BrickColor", "LightPosition" };

	private SimpleUniverse univ = null;
	//private View view;
	//private BranchGroup transpObj;
	private BranchGroup scene = null;
	private int shaderSelected = DIMPLE_SHADER;
	private float density = 16.0f;
	private int color = GOLD;

	private Color3f eColor = new Color3f(0.2f, 0.2f, 0.2f);
	private Color3f sColor = new Color3f(0.8f, 0.8f, 0.8f);
	private Color3f objColor = new Color3f(0.6f, 0.6f, 0.6f);
	private Color3f bgColor = new Color3f(0.05f, 0.05f, 0.2f);
	private Color3f gold = new Color3f(0.7f, 0.6f, 0.18f);
	private Color3f silver = new Color3f(0.75f, 0.75f, 0.75f);

	// Handlers for doing update
	private ShaderAppearance sApp1 = null;
	private ShaderAppearance sApp2 = null;
	private ShaderAppearance sApp3 = null;
	private ShaderAppearance sApp4 = null;
	private ShaderProgram sp1 = null;
	private ShaderProgram sp2 = null;
	private ShaderProgram sp3 = null;
	private ShaderProgram sp4 = null;
	private ShaderAttributeSet sas1 = null;
	private ShaderAttributeSet sas2 = null;
	private ShaderAttributeObject sao1 = null;
	private ShaderAttributeObject sao2 = null;
	private Sphere sphere = null;
	private Shape3D s3d = null;

	private Material createMaterial()
	{
		Material m;
		m = new Material(objColor, eColor, objColor, sColor, 100.0f);
		m.setLightingEnable(true);
		return m;
	}

	private static ShaderProgram createGLSLShaderProgram(int index)
	{
		String vertexProgram = null;
		String fragmentProgram = null;

		try
		{
			switch (index)
			{
			case DIMPLE_SHADER:
				vertexProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/dimple.vert"));
				fragmentProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/dimple.frag"));
				break;
			case BRICK_SHADER:
				vertexProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/aabrick.vert"));
				fragmentProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/aabrick.frag"));
				break;
			case WOOD_SHADER:
				vertexProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/wood.vert"));
				fragmentProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/wood.frag"));
				break;
			case POLKADOT3D_SHADER:
				vertexProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/polkadot3d.vert"));
				fragmentProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/polkadot3d.frag"));
				break;
			default:
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		Shader[] shaders = new Shader[2];
		shaders[0] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_VERTEX, vertexProgram);
		shaders[1] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_FRAGMENT, fragmentProgram);
		ShaderProgram shaderProgram = new GLSLShaderProgram();
		shaderProgram.setShaders(shaders);
		return shaderProgram;
	}

	private ShaderAttributeSet createShaderAttributeSet(int index)
	{
		ShaderAttributeSet shaderAttributeSet = new ShaderAttributeSet();
		ShaderAttributeObject shaderAttribute = null;

		switch (index)
		{
		case DIMPLE_SHADER:
			//  "Density", "Size", "Scale", "Color", "LightPosition"
			shaderAttribute = new ShaderAttributeValue("Size", new Float(0.25));
			shaderAttributeSet.put(shaderAttribute);
			shaderAttribute = new ShaderAttributeValue("LightPosition", new Point3f(0.0f, 0.0f, 0.5f));
			shaderAttributeSet.put(shaderAttribute);

			sao1 = new ShaderAttributeValue("Density", new Float(density));
			sao1.setCapability(ShaderAttributeObject.ALLOW_VALUE_READ);
			sao1.setCapability(ShaderAttributeObject.ALLOW_VALUE_WRITE);
			shaderAttributeSet.put(sao1);

			if (color == GOLD)
			{
				sao2 = new ShaderAttributeValue("Color", gold);
			}
			else if (color == SILVER)
			{
				sao2 = new ShaderAttributeValue("Color", silver);
			}
			sao2.setCapability(ShaderAttributeObject.ALLOW_VALUE_READ);
			sao2.setCapability(ShaderAttributeObject.ALLOW_VALUE_WRITE);
			shaderAttributeSet.put(sao2);
			break;

		case BRICK_SHADER:
			// "BrickColor", "LightPosition"
			shaderAttribute = new ShaderAttributeValue("BrickColor", new Color3f(1.0f, 0.3f, 0.2f));
			shaderAttributeSet.put(shaderAttribute);
			shaderAttribute = new ShaderAttributeValue("LightPosition", new Point3f(0.0f, 0.0f, 0.5f));
			shaderAttributeSet.put(shaderAttribute);
			break;
		default:
			assert false;
		}
		return shaderAttributeSet;
	}

	private ShaderAppearance createShaderAppearance()
	{
		ShaderAppearance sApp = new ShaderAppearance();
		sApp.setMaterial(createMaterial());
		return sApp;
	}

	private BranchGroup createSubSceneGraph()
	{
		// Create the sub-root of the branch graph
		BranchGroup subRoot = new BranchGroup();

		//
		// Create 1 spheres with a GLSLShader and add it into the scene graph.
		//
		sApp1 = createShaderAppearance();
		sApp1.setCapability(ShaderAppearance.ALLOW_SHADER_PROGRAM_READ);
		sApp1.setCapability(ShaderAppearance.ALLOW_SHADER_PROGRAM_WRITE);
		sApp1.setCapability(ShaderAppearance.ALLOW_SHADER_ATTRIBUTE_SET_READ);
		sApp1.setCapability(ShaderAppearance.ALLOW_SHADER_ATTRIBUTE_SET_WRITE);

		sp1 = createGLSLShaderProgram(1);
		sp1.setShaderAttrNames(shaderAttrNames1);
		sas1 = createShaderAttributeSet(1);
		sas1.setCapability(ShaderAttributeSet.ALLOW_ATTRIBUTES_READ);
		sas1.setCapability(ShaderAttributeSet.ALLOW_ATTRIBUTES_WRITE);
		sApp1.setShaderProgram(sp1);
		sApp1.setShaderAttributeSet(sas1);

		// Setup Brick shader
		sp2 = createGLSLShaderProgram(2);
		sp2.setShaderAttrNames(shaderAttrNames2);
		sas2 = createShaderAttributeSet(2);
		sApp2 = createShaderAppearance();
		sApp2.setShaderProgram(sp2);
		sApp2.setShaderAttributeSet(sas2);

		// Setup Wood shader
		sp3 = createGLSLShaderProgram(3);
		sApp3 = createShaderAppearance();
		sApp3.setShaderProgram(sp3);

		// Setup Polkadot3d shader
		sp4 = createGLSLShaderProgram(4);
		sApp4 = createShaderAppearance();
		sApp4.setShaderProgram(sp4);

		sphere = new Sphere(1.5f, Sphere.GENERATE_NORMALS, 200, null);
		SphereGLSL.makeNIO(sphere);
		s3d = sphere.getShape();
		s3d.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
		s3d.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		s3d.setAppearance(sApp1);

		TransformGroup objTG;
		Transform3D t = new Transform3D();
		t.set(new Vector3d(0.0, 0.0, 0.0));
		objTG = new TransformGroup(t);
		objTG.addChild(sphere);
		subRoot.addChild(objTG);

		return subRoot;
	}


	private BranchGroup createSceneGraph(int selectedScene)
	{
		// Create the root of the branch graph
		BranchGroup objRoot = new BranchGroup();
		objRoot.setCapability(BranchGroup.ALLOW_DETACH);

		// Create a Transformgroup to scale all objects so they
		// appear in the scene.
		TransformGroup objScale = new TransformGroup();
		Transform3D t3d = new Transform3D();
		t3d.setScale(0.4);
		objScale.setTransform(t3d);
		objRoot.addChild(objScale);

		// Create a bounds for the background and lights
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

		// Set up the background
		Background bg = new Background(bgColor);
		bg.setApplicationBounds(bounds);
		objScale.addChild(bg);

		objScale.addChild(createSubSceneGraph());

		// Create a position interpolator and attach it to the view
		// platform
		TransformGroup vpTrans = univ.getViewingPlatform().getViewPlatformTransform();
		Transform3D axisOfTranslation = new Transform3D();
		Alpha transAlpha = new Alpha(-1, Alpha.INCREASING_ENABLE | Alpha.DECREASING_ENABLE, 0, 0, 5000, 0, 0, 5000, 0, 0);
		axisOfTranslation.rotY(-Math.PI / 2.0);
		PositionInterpolator translator = new PositionInterpolator(transAlpha, vpTrans, axisOfTranslation, 2.0f, 3.5f);
		translator.setSchedulingBounds(bounds);
		objScale.addChild(translator);

		// Let Java 3D perform optimizations on this scene graph.
		objRoot.compile();

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
				ShaderTestGLSL.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(ShaderTestGLSL.this, error.toString(), Toast.LENGTH_LONG ).show();
					}
				});
			}
		});

		ViewingPlatform viewingPlatform = univ.getViewingPlatform();
		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		viewingPlatform.setNominalViewingTransform();

		//view = univ.getViewer().getView();

		return c;
	}

	// ----------------------------------------------------------------

	private Canvas3D c = null;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shadertestglsl);

		SimpleShaderAppearance.setVersionES300();
		// Initialize the GUI components
		initComponents();

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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// ----------------------------------------------------------------

	private void initComponents()
	{
		zeroButton = findViewById(R.id.zeroButton);
		halfButton = findViewById(R.id.halfButton);
		fullButton = findViewById(R.id.fullButton);
		goldButton = findViewById(R.id.goldButton);
		silverButton = findViewById(R.id.silverButton);
		DetachButton = findViewById(R.id.DetachButton);
		AttachButton = findViewById(R.id.AttachButton);
		replaceSPButton = findViewById(R.id.replaceSPButton);

		fullButton.setSelected(true);

		goldButton.setSelected(true);

		DetachButton.setSelected(true);

		replaceSPButton.setEnabled(false);
	}


	public void silverButtonActionPerformed(View view)
	{
		color = SILVER;
		if (scene != null)
		{
			sao2.setValue(silver);
		}
	}

	public void goldButtonActionPerformed(View view)
	{
		color = GOLD;
		if (scene != null)
		{
			sao2.setValue(gold);
		}
	}

	public void replaceSPButtonActionPerformed(View view)
	{
		if (shaderSelected != DIMPLE_SHADER)
		{
			goldButton.setEnabled(false);
			silverButton.setEnabled(false);
			zeroButton.setEnabled(false);
			halfButton.setEnabled(false);
			fullButton.setEnabled(false);
		}

		switch (shaderSelected)
		{
		case DIMPLE_SHADER:
			s3d.setAppearance(sApp1);
			goldButton.setEnabled(true);
			silverButton.setEnabled(true);
			zeroButton.setEnabled(true);
			halfButton.setEnabled(true);
			fullButton.setEnabled(true);
			shaderSelected = BRICK_SHADER;
			break;
		case BRICK_SHADER:
			s3d.setAppearance(sApp2);
			shaderSelected = WOOD_SHADER;
			break;
		case WOOD_SHADER:
			s3d.setAppearance(sApp3);
			shaderSelected = POLKADOT3D_SHADER;
			break;
		case POLKADOT3D_SHADER:
			s3d.setAppearance(sApp4);
			shaderSelected = DIMPLE_SHADER;
			break;
		default:
			assert false;
		}

	}

	public void fullButtonActionPerformed(View view)
	{
		density = 16.0f;
		if (scene != null)
		{
			sao1.setValue(new Float(density));
		}
	}

	public void DetachButtonActionPerformed(View view)
	{
		if (scene != null)
		{
			scene.detach();
			scene = null;
			replaceSPButton.setEnabled(false);
			goldButton.setEnabled(true);
			silverButton.setEnabled(true);
			zeroButton.setEnabled(true);
			halfButton.setEnabled(true);
			fullButton.setEnabled(true);
			shaderSelected = DIMPLE_SHADER;
		}
	}

	public void AttachButtonActionPerformed(View view)
	{
		if (scene == null)
		{
			scene = createSceneGraph(1);
			univ.addBranchGraph(scene);
			replaceSPButton.setEnabled(true);
			shaderSelected = BRICK_SHADER;
		}
	}

	public void halfButtonActionPerformed(View view)
	{
		density = 8.0f;
		if (scene != null)
		{
			sao1.setValue(new Float(density));
		}
	}

	public void zeroButtonActionPerformed(View view)
	{
		density = 0.0f;
		if (scene != null)
		{
			sao1.setValue(new Float(density));
		}

	}

	private ToggleButton AttachButton;
	private ToggleButton DetachButton;
	private RadioButton fullButton;
	private RadioButton goldButton;
	private RadioButton halfButton;
	private Button replaceSPButton;
	private RadioButton silverButton;
	private RadioButton zeroButton;

}
