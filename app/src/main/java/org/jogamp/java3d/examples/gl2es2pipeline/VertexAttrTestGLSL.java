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
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jogamp.newt.opengl.GLWindow;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.GLSLShaderProgram;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.J3DBuffer;
import org.jogamp.java3d.Shader;
import org.jogamp.java3d.ShaderAppearance;
import org.jogamp.java3d.ShaderError;
import org.jogamp.java3d.ShaderErrorListener;
import org.jogamp.java3d.ShaderProgram;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.SourceCodeShader;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.TriangleArray;
import org.jogamp.java3d.examples.alternate_appearance.AlternateAppearanceBoundsTest;
import org.jogamp.java3d.examples.java3dexamples.R;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.shader.StringIO;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;

import jogamp.newt.driver.android.NewtBaseFragment;
import jogamp.newt.driver.android.NewtBaseFragmentActivity;

public class VertexAttrTestGLSL extends NewtBaseFragmentActivity {

	SimpleUniverse univ = null;
	BranchGroup scene = null;

	public BranchGroup createSceneGraph(boolean hasVertexAttrs)
	{
		// Bounds for BG and behavior
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

		// Create the root of the branch graph
		BranchGroup objRoot = new BranchGroup();
		objRoot.setCapability(BranchGroup.ALLOW_DETACH);

		// Set up the background
		Color3f bgColor = new Color3f(0.1f, 0.1f, 0.1f);
		Background bg = new Background(bgColor);
		bg.setApplicationBounds(bounds);
		objRoot.addChild(bg);

		// Create the TransformGroup node and initialize it to the
		// identity. Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at run time. Add it to
		// the root of the subgraph.
		TransformGroup objTrans = new TransformGroup();
		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objRoot.addChild(objTrans);

		// Create a simple Shape3D node; add it to the scene graph.
		objTrans.addChild(new MyShape(hasVertexAttrs));

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
				VertexAttrTestGLSL.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(VertexAttrTestGLSL.this, error.toString(), Toast.LENGTH_LONG ).show();
					}
				});
			}
		});

		ViewingPlatform viewingPlatform = univ.getViewingPlatform();
		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		viewingPlatform.setNominalViewingTransform();

		return c;
	}

	// ----------------------------------------------------------------

	private Canvas3D c = null;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SimpleShaderAppearance.setVersionES300();
		setContentView(R.layout.vertexattrtestglsl);

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

	static class MyShape extends Shape3D
	{

		// Coordinate data
		private static final float[] coords = { 0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f, };

		private static final int[] sizes = { 1, 3 };
		private static final float[] weights = { 0.45f, 0.15f, 0.95f, };
		private static final float[] temps = { 1.0f, 0.5f, 0.5f, 0.5f, 1.0f, 0.5f, 0.5f, 0.5f, 1.0f, };

		private static final String[] vaNames = { "weight", "temperature" };

		J3DBuffer createDirectFloatBuffer(float[] arr)
		{
			ByteOrder order = ByteOrder.nativeOrder();

			FloatBuffer nioBuf = ByteBuffer.allocateDirect(arr.length * 4).order(order).asFloatBuffer();
			nioBuf.put(arr);
			return new J3DBuffer(nioBuf);
		}

		MyShape(boolean hasVertexAttrs)
		{

			int vertexFormat = GeometryArray.COORDINATES;
			int vertexAttrCount = 0;
			int[] vertexAttrSizes = null;
			String[] vertexAttrNames = null;
			String[] shaderAttrNames = null;

			if (hasVertexAttrs)
			{
				vertexFormat |= GeometryArray.VERTEX_ATTRIBUTES;
				vertexAttrCount = vaNames.length;
				vertexAttrSizes = sizes;
				vertexAttrNames = vaNames;
			}
			
			//GL2ES2: requires by reference
			vertexFormat |= GeometryArray.BY_REFERENCE;

			TriangleArray tri = new TriangleArray(6, vertexFormat, 0, null, vertexAttrCount, vertexAttrSizes);
			tri.setValidVertexCount(3);
			//tri.setCoordinates(0, coords);
			tri.setCoordRefFloat(coords);

			if (hasVertexAttrs)
			{
				//tri.setVertexAttrs(0, 0, weights);
				//tri.setVertexAttrs(1, 0, temps);
				
				tri.setVertexAttrRefFloat(0, weights);
				tri.setVertexAttrRefFloat(1, temps);

				String vertexProgram = null;
				String fragmentProgram = null;
				try
				{
					vertexProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/vertexshader.vert"));
					fragmentProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/vertexshader.frag"));
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
				shaderProgram.setVertexAttrNames(vertexAttrNames);
				shaderProgram.setShaderAttrNames(shaderAttrNames);

				ShaderAppearance app = new ShaderAppearance();
				app.setShaderProgram(shaderProgram);

				this.setGeometry(tri);

				this.setAppearance(app);
			}
			else
			{
				this.setGeometry(tri);
				this.setAppearance(new SimpleShaderAppearance());
			}
		}
	}

	// ----------------------------------------------------------------


	private void initComponents()
	{
		vertexAttrsBox = findViewById(R.id.vertexAttrsBox);
		vertexAttrsBox.setSelected(true);
	}

	public void destroyButtonActionPerformed(View view)
	{
		if (scene != null)
		{
			univ.getLocale().removeBranchGraph(scene);
			scene = null;
		}
	}

	public void createButtonActionPerformed(View view)
	{
		if (scene == null)
		{
			boolean hasVertexAttrs = vertexAttrsBox.isSelected();
			scene = createSceneGraph(hasVertexAttrs);
			univ.addBranchGraph(scene);
		}
	}

	private CheckBox vertexAttrsBox;
}
