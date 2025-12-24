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
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.GLSLShaderProgram;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.Shader;
import org.jogamp.java3d.ShaderAppearance;
import org.jogamp.java3d.ShaderError;
import org.jogamp.java3d.ShaderErrorListener;
import org.jogamp.java3d.ShaderProgram;
import org.jogamp.java3d.SourceCodeShader;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.loaders.IncorrectFormatException;
import org.jogamp.java3d.loaders.ParsingErrorException;
import org.jogamp.java3d.loaders.Scene;
import org.jogamp.java3d.loaders.objectfile.ObjectFile;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.shader.StringIO;
import org.jogamp.java3d.utils.universe.PlatformGeometry;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3f;

import jogamp.newt.driver.android.NewtBaseActivity;

/**
 * Simple Java 3D example program to display an .obj object with shader programs.
 */
public class ObjLoadGLSL extends NewtBaseActivity {

	private String shaderName = "polkadot3d";
	private boolean spin = false;
	private boolean noTriangulate = false;
	private boolean noStripify = false;
	private double creaseAngle = 60.0;
	private URL filename = null;

	private SimpleUniverse univ = null;
	private BranchGroup scene = null;

	public BranchGroup createSceneGraph()
	{
		// Create the root of the branch graph
		BranchGroup objRoot = new BranchGroup();

		// Create a Transformgroup to scale all objects so they
		// appear in the scene.
		TransformGroup objScale = new TransformGroup();
		Transform3D t3d = new Transform3D();
		t3d.setScale(0.7);
		objScale.setTransform(t3d);
		objRoot.addChild(objScale);

		// Create the transform group node and initialize it to the
		// identity.  Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at runtime.  Add it to the
		// root of the subgraph.
		TransformGroup objTrans = new TransformGroup();
		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		objScale.addChild(objTrans);

		// Set vertex and fragment shader program for all Shape3D nodes in scene
		String vertexProgram = null;
		String fragmentProgram = null;
		try
		{
			vertexProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/" + shaderName + ".vert"));
			fragmentProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/" + shaderName + ".frag"));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		Shader[] shaders = new Shader[2];
		shaders[0] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_VERTEX, vertexProgram);
		shaders[1] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_FRAGMENT, fragmentProgram);
		final ShaderProgram shaderProgram = new GLSLShaderProgram();
		shaderProgram.setShaders(shaders);

		int flags = ObjectFile.RESIZE;
		if (!noTriangulate)
			flags |= ObjectFile.TRIANGULATE;
		if (!noStripify)
			flags |= ObjectFile.STRIPIFY;
		ObjectFile f = new ObjectFile(flags, (float) (creaseAngle * Math.PI / 180.0)) {
			@Override
			public Appearance createAppearance()
			{
				ShaderAppearance sa = new ShaderAppearance();
				sa.setShaderProgram(shaderProgram);
				return sa;
			}
		};
		Scene s = null;
		try	{
			s = f.load(filename);
		} catch (FileNotFoundException e) {
			System.err.println(e);
			System.exit(1);
		} catch (ParsingErrorException e) {
			System.err.println(e);
			System.exit(1);
		} catch (IncorrectFormatException e) {
			System.err.println(e);
			System.exit(1);
		}

		objTrans.addChild(s.getSceneGroup());

		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

		if (spin)
		{
			Transform3D yAxis = new Transform3D();
			Alpha rotationAlpha = new Alpha(-1, Alpha.INCREASING_ENABLE, 0, 0, 4000, 0, 0, 0, 0, 0);

			RotationInterpolator rotator = new RotationInterpolator(rotationAlpha, objTrans, yAxis, 0.0f, (float) Math.PI * 2.0f);
			rotator.setSchedulingBounds(bounds);
			objTrans.addChild(rotator);
		}

		// Set up the background
		Color3f bgColor = new Color3f(0.05f, 0.05f, 0.5f);
		Background bgNode = new Background(bgColor);
		bgNode.setApplicationBounds(bounds);
		objRoot.addChild(bgNode);

		return objRoot;
	}

	private Canvas3D createUniverse()
	{
		// Get the preferred graphics configuration for the default screen
		//GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

		// Create a Canvas3D using the preferred configuration
		Canvas3D canvas3d = new Canvas3D();

		// Create simple universe with view branch
		univ = new SimpleUniverse(canvas3d);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

		// Add a ShaderErrorListener
		univ.addShaderErrorListener(new ShaderErrorListener() {
			@Override
			public void errorOccurred(ShaderError error)
			{
				error.printVerbose();
				ObjLoadGLSL.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(ObjLoadGLSL.this, error.toString(), Toast.LENGTH_LONG ).show();
					}
				});
			}
		});

		// add mouse behaviors to the ViewingPlatform
		ViewingPlatform viewingPlatform = univ.getViewingPlatform();

		PlatformGeometry pg = new PlatformGeometry();

		// Set up the ambient light
		Color3f ambientColor = new Color3f(0.3f, 0.3f, 0.3f);
		AmbientLight ambientLightNode = new AmbientLight(ambientColor);
		ambientLightNode.setInfluencingBounds(bounds);
		pg.addChild(ambientLightNode);

		// Set up the directional lights
		Color3f light1Color = new Color3f(1.0f, 1.0f, 0.9f);
		Vector3f light1Direction = new Vector3f(1.0f, 1.0f, 1.0f);
		Color3f light2Color = new Color3f(1.0f, 1.0f, 1.0f);
		Vector3f light2Direction = new Vector3f(-1.0f, -1.0f, -1.0f);

		DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
		light1.setInfluencingBounds(bounds);
		pg.addChild(light1);

		DirectionalLight light2 = new DirectionalLight(light2Color, light2Direction);
		light2.setInfluencingBounds(bounds);
		pg.addChild(light2);

		viewingPlatform.setPlatformGeometry(pg);

		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		viewingPlatform.setNominalViewingTransform();

		/*if (!spin)
		{
			OrbitBehavior orbit = new OrbitBehavior(canvas3d, OrbitBehavior.REVERSE_ALL);
			orbit.setSchedulingBounds(bounds);
			viewingPlatform.setViewPlatformBehavior(orbit);
		}*/

		// Ensure at least 5 msec per frame (i.e., < 200Hz)
		univ.getViewer().getView().setMinimumFrameCycleTime(5);

		return canvas3d;
	}

	private static void usage()
	{
		System.out.println("Usage: java ObjLoadGLSL [-s] [-S shaderName] [-n] [-t] [-c degrees] <.obj file>");
		System.out.println("  -s Spin (no user interaction)");
		System.out.println("  -S Set shader name (default is 'simple')");
		System.out.println("  -n No triangulation");
		System.out.println("  -t No stripification");
		System.out.println("  -c Set crease angle for normal generation (default is 60 without");
		System.out.println("     smoothing group info, otherwise 180 within smoothing groups)");
		System.exit(0);
	} // End of usage


	// ----------------------------------------------------------------

	private Canvas3D c = null;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SimpleShaderAppearance.setVersionES300();

		// just make it spin
		String[] args = new String[]{"-s"};

		if (args.length != 0)
		{
			for (int i = 0; i < args.length; i++)
			{
				if (args[i].startsWith("-"))
				{
					if (args[i].equals("-s"))
					{
						spin = true;
					}
					else if (args[i].equals("-n"))
					{
						noTriangulate = true;
					}
					else if (args[i].equals("-t"))
					{
						noStripify = true;
					}
					else if (args[i].equals("-c"))
					{
						if (i < args.length - 1)
						{
							creaseAngle = (new Double(args[++i])).doubleValue();
						}
						else
							usage();
					}
					else if (args[i].equals("-S"))
					{
						if (i < args.length - 1)
						{
							shaderName = args[++i];
						}
						else
							usage();
					}
					else
					{
						usage();
					}
				}
				else
				{
					try
					{
						if ((args[i].indexOf("file:") == 0) || (args[i].indexOf("http") == 0))
						{
							filename = new URL(args[i]);
						}
						else if (args[i].charAt(0) != '/')
						{
							filename = new URL("file:./" + args[i]);
						}
						else
						{
							filename = new URL("file:" + args[i]);
						}
					}
					catch (MalformedURLException e)
					{
						System.err.println(e);
						System.exit(1);
					}
				}
			}
		}

		if (filename == null)
		{
			String[] possibleObjFiles = new String[]{
					"galleon.obj","beethoven.obj","hand1.obj","hand2.obj","hand3.obj","p51_mustang.obj",
			};


			String objfile = "/resources/geometry/"+possibleObjFiles[(int) (Math.random()*possibleObjFiles.length)];
			filename = getClass().getResource(objfile);
			if (filename == null)
			{
				System.err.println(objfile + " not found");
				System.exit(1);
			}
		}

		String[] possibleShaderNames = new String[]{
				"polkadot3d","aabrick", "gouraud","phong","toon","wood",
		};
		shaderName = possibleShaderNames[(int) (Math.random()*possibleShaderNames.length)];

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
