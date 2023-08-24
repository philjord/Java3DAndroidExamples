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
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.GLSLShaderProgram;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.Shader;
import org.jogamp.java3d.ShaderAppearance;
import org.jogamp.java3d.ShaderAttribute;
import org.jogamp.java3d.ShaderAttributeSet;
import org.jogamp.java3d.ShaderAttributeValue;
import org.jogamp.java3d.ShaderError;
import org.jogamp.java3d.ShaderErrorListener;
import org.jogamp.java3d.ShaderProgram;
import org.jogamp.java3d.SourceCodeShader;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.TextureUnitState;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.shader.StringIO;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;

import javaawt.image.VMBufferedImage;
import javaawt.imageio.VMImageIO;
import jogamp.newt.driver.android.NewtBaseActivity;

public class EnvironmentMappingGLSL extends NewtBaseActivity {

	private URL textureURL = null;
	private static final int NUM_TEX_UNITS = 1;
	private static final int TEX_UNIT = 0;

	SimpleUniverse univ = null;

	public BranchGroup createSceneGraph()
	{
		// Create the root of the branch graph
		BranchGroup objRoot = new BranchGroup();

		// Create the TransformGroup node and initialize it to the
		// identity. Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at run time. Add it to
		// the root of the subgraph.
		TransformGroup objTrans = new TransformGroup();
		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objRoot.addChild(objTrans);

		// Create texture object
		textureURL = getClass().getResource("/resources/images/duke-gears.jpg");
		Texture tex = new TextureLoader(textureURL, this).getTexture();
		// Create the shader program
		String vertexProgram = null;
		String fragmentProgram = null;
		try
		{
			vertexProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/envmap.vert"));
			fragmentProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/envmap.frag"));
		}
		catch (IOException e)
		{
			System.err.println(e);
		}
		Shader[] shaders = new Shader[2];
		shaders[0] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_VERTEX, vertexProgram);
		shaders[1] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_FRAGMENT, fragmentProgram);
		final String[] shaderAttrNames = { "LightPos", "BaseColor", "MixRatio", "EnvMap", };
		final Object[] shaderAttrValues = { new Point3f(1.0f, -1.0f, 2.0f), new Color3f(0.2f, 0.9f, 0.5f), new Float(0.4f),
				new Integer(TEX_UNIT), };
		ShaderProgram shaderProgram = new GLSLShaderProgram();
		shaderProgram.setShaders(shaders);
		shaderProgram.setShaderAttrNames(shaderAttrNames);

		// Create the shader attribute set
		ShaderAttributeSet shaderAttributeSet = new ShaderAttributeSet();
		for (int i = 0; i < shaderAttrNames.length; i++)
		{
			ShaderAttribute shaderAttribute = new ShaderAttributeValue(shaderAttrNames[i], shaderAttrValues[i]);
			shaderAttributeSet.put(shaderAttribute);
		}

		// Create shader appearance to hold the shader program and
		// shader attributes
		ShaderAppearance app = new ShaderAppearance();
		app.setShaderProgram(shaderProgram);
		app.setShaderAttributeSet(shaderAttributeSet);

		// Put the texture in specified texture unit
		TextureUnitState[] tus = new TextureUnitState[NUM_TEX_UNITS];
		tus[TEX_UNIT] = new TextureUnitState();
		tus[TEX_UNIT].setTexture(tex);
		app.setTextureUnitState(tus);

		// Create a Sphere object using the shader appearance,
		// and add it into the scene graph.
		Sphere sph = new Sphere(0.4f, Sphere.GENERATE_NORMALS, 20, app);
		SphereGLSL.makeNIO(sph);
		objTrans.addChild(sph);

		// Create a new Behavior object that will perform the
		// desired operation on the specified transform and add
		// it into the scene graph.
		Transform3D yAxis = new Transform3D();
		Alpha rotationAlpha = new Alpha(-1, 4000);

		RotationInterpolator rotator = new RotationInterpolator(rotationAlpha, objTrans, yAxis, 0.0f, (float) Math.PI * 2.0f);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
		rotator.setSchedulingBounds(bounds);
		objRoot.addChild(rotator);

		// Have Java 3D perform optimizations on this scene graph.
		//objRoot.compile();

		return objRoot;
	}

	 

	private Canvas3D initScene()
	{
		//GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

		Canvas3D c = new Canvas3D();

		BranchGroup scene = createSceneGraph();
		univ = new SimpleUniverse(c);

		// Add a ShaderErrorListener
		univ.addShaderErrorListener(new ShaderErrorListener() {
			@Override
			public void errorOccurred(ShaderError error)
			{
				error.printVerbose();
				EnvironmentMappingGLSL.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(EnvironmentMappingGLSL.this, error.toString(), Toast.LENGTH_LONG ).show();
					}
				});
			}
		});

		ViewingPlatform viewingPlatform = univ.getViewingPlatform();
		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		viewingPlatform.setNominalViewingTransform();

		univ.addBranchGraph(scene);

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

		// Create the scene and add the Canvas3D to the drawing panel
		c = initScene();

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
