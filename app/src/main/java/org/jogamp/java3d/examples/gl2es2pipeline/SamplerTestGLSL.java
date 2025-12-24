package org.jogamp.java3d.examples.gl2es2pipeline;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.GLSLShaderProgram;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.J3DBuffer;
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
import org.jogamp.java3d.TriangleStripArray;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.shader.StringIO;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javaawt.imageio.VMImageIO;
import jogamp.newt.driver.android.NewtBaseActivity;

public class SamplerTestGLSL extends NewtBaseActivity {
    private BranchGroup scene = null;
    private Canvas3D canvas3D = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        javaawt.imageio.ImageIO.installBufferedImageImpl(VMImageIO.class);

        SimpleShaderAppearance.setVersionES300();

        // Create a Canvas3D using the default configuration
        canvas3D = new Canvas3D();

        // Create simple universe with view branch
        univ = new SimpleUniverse(canvas3D);
        // Add a ShaderErrorListener
        univ.addShaderErrorListener(new ShaderErrorListener() {
            @Override
            public void errorOccurred(ShaderError error)
            {
                error.printVerbose();
                SamplerTestGLSL.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SamplerTestGLSL.this, error.toString(), Toast.LENGTH_LONG ).show();
                    }
                });
            }
        });

        // This will move the ViewPlatform back a bit so the objects in the scene can be viewed.
        univ.getViewingPlatform().setNominalViewingTransform();

        // Ensure at least 5 msec per frame (i.e., < 200Hz)
        univ.getViewer().getView().setMinimumFrameCycleTime(5);

        // make up an interesting wee scene
        scene = createSceneGraph();

        // add the scene to the Java3D universe so it can be traversed and rendered
        univ.addBranchGraph(scene);

        // make the gl window the content of this app
        this.setContentView(this.getWindow(), canvas3D.getGLWindow());
    }

    // the 4 methods below are life cycle management to keep the app stable and well behaved
    @Override
    public void onResume() {
        canvas3D.getGLWindow().setVisible(true);
        canvas3D.startRenderer();
        super.onResume();
    }

    @Override
    public void onPause() {
        canvas3D.stopRenderer();
        canvas3D.removeNotify();
        canvas3D.getGLWindow().setVisible(false);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        canvas3D.stopRenderer();
        canvas3D.removeNotify();
        canvas3D.getGLWindow().destroy();
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

    private static String cloudTexName = "/resources/images/bg.jpg";
    private static String earthTexName = "/resources/images/earth.jpg";

    private URL cloudURL = null;
    private URL earthURL = null;
    private static final int CLOUD = 0;
    private static final int EARTH = 1;

    SimpleUniverse univ = null;

    public BranchGroup createSceneGraph() {
        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        // Create the TransformGroup node and initialize it to the
        // identity. Enable the TRANSFORM_WRITE capability so that
        // our behavior code can modify it at run time. Add it to
        // the root of the subgraph.
        TransformGroup objTrans = new TransformGroup();
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRoot.addChild(objTrans);

        // Create texture objects
        cloudURL = SamplerTestGLSL.class.getResource(cloudTexName);
        Texture cloudTex = new TextureLoader(cloudURL, this).getTexture();
        earthURL = SamplerTestGLSL.class.getResource(earthTexName);
        Texture earthTex = new TextureLoader(earthURL, this).getTexture();

        // Create the shader program
        String vertexProgram = null;
        String fragmentProgram = null;
        try {
            vertexProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/multitex.vert"));
            fragmentProgram = StringIO.readFully(SamplerTestGLSL.class.getResource("/shaders/multitex.frag"));
        } catch (IOException e) {
            System.err.println(e);
        }
        Shader[] shaders = new Shader[2];
        shaders[0] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_VERTEX, vertexProgram);
        shaders[1] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_FRAGMENT, fragmentProgram);
        final String[] shaderAttrNames = {"cloudFactor", "cloudTex", "earthTex",};
        final Object[] shaderAttrValues = {new Float(0.6f), new Integer(0), new Integer(1),};
        ShaderProgram shaderProgram = new GLSLShaderProgram();
        shaderProgram.setShaders(shaders);
        shaderProgram.setShaderAttrNames(shaderAttrNames);

        // Create the shader attribute set
        ShaderAttributeSet shaderAttributeSet = new ShaderAttributeSet();
        for (int i = 0; i < shaderAttrNames.length; i++) {
            ShaderAttribute shaderAttribute = new ShaderAttributeValue(shaderAttrNames[i], shaderAttrValues[i]);
            shaderAttributeSet.put(shaderAttribute);
        }

        // Create shader appearance to hold the shader program and
        // shader attributes
        ShaderAppearance app = new ShaderAppearance();
        app.setShaderProgram(shaderProgram);
        app.setShaderAttributeSet(shaderAttributeSet);

        // GL2ES2: Tex coord gen done in shader now
        //Vector4f plane0S = new Vector4f(3.0f, 1.5f, 0.3f, 0.0f);
        //Vector4f plane0T = new Vector4f(1.0f, 2.5f, 0.24f, 0.0f);
        //TexCoordGeneration tcg0 = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR, TexCoordGeneration.TEXTURE_COORDINATE_2, plane0S,
        //		plane0T);
        //TexCoordGeneration tcg1 = new TexCoordGeneration(TexCoordGeneration.SPHERE_MAP, TexCoordGeneration.TEXTURE_COORDINATE_2);

        // Put the textures in unit 0,1
        TextureUnitState[] tus = new TextureUnitState[2];
        tus[CLOUD] = new TextureUnitState();
        tus[CLOUD].setTexture(cloudTex);

        // GL2ES2: Tex coord gen done in shader now
        //tus[CLOUD].setTexCoordGeneration(tcg0);

        tus[EARTH] = new TextureUnitState();
        tus[EARTH].setTexture(earthTex);

        // GL2ES2: Tex coord gen done in shader now
        //tus[EARTH].setTexCoordGeneration(tcg1);

        app.setTextureUnitState(tus);

        // Create a Sphere object using the shader appearance,
        // and add it into the scene graph.
        Sphere sph = new Sphere(0.4f, Sphere.GENERATE_NORMALS, 30, app);

        makeNIO(sph);
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

        return objRoot;
    }

    public static void makeNIO(Sphere sph) {
        //Make it NIO
        TriangleStripArray geo = (TriangleStripArray) sph.getShape().getGeometry();
        int[] stripVertexCounts = new int[geo.getNumStrips()];
        geo.getStripVertexCounts(stripVertexCounts);
        TriangleStripArray newGeo = new TriangleStripArray(geo.getVertexCount(), GeometryArray.COORDINATES | GeometryArray.NORMALS
                //| GeometryArray.TEXTURE_COORDINATE_2
                | GeometryArray.USE_NIO_BUFFER | GeometryArray.BY_REFERENCE, stripVertexCounts);

        float[] coords = new float[geo.getValidVertexCount() * 3];
        geo.getCoordinates(0, coords);
        newGeo.setCoordRefBuffer(new J3DBuffer(makeFloatBuffer(coords)));
        float[] norms = new float[geo.getValidVertexCount() * 3];
        geo.getNormals(0, norms);
        newGeo.setNormalRefBuffer(new J3DBuffer(makeFloatBuffer(norms)));
        sph.getShape().setGeometry(newGeo);
    }

    private static FloatBuffer makeFloatBuffer(float[] arr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(arr);
        fb.position(0);
        return fb;
    }

}


