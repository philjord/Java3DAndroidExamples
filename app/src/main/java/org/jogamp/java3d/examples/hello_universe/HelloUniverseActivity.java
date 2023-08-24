package org.jogamp.java3d.examples.hello_universe;

import android.os.Bundle;
import android.util.Log;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.shader.Cube;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;


import java.io.PrintStream;

import jogamp.newt.driver.android.NewtBaseActivity;

public class HelloUniverseActivity extends NewtBaseActivity {
    private SimpleUniverse universe = null;
    private BranchGroup scene = null;
    private Canvas3D canvas3D = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // get system out and err to log for logcat
        System.setOut(new PrintStream(System.out, true) {
            public void print(String s) {
                Log.w("sysout", s);

            }
        });
        System.setOut(new PrintStream(System.err, true) {
            public void print(String s) {
                Log.w("syserr", s);

            }
        });

        SimpleShaderAppearance.setVersionES300();

        // Create a Canvas3D using the default configuration
        canvas3D = new Canvas3D();

        // Create simple universe with view branch
        universe = new SimpleUniverse(canvas3D);

        // This will move the ViewPlatform back a bit so the objects in the scene can be viewed.
        universe.getViewingPlatform().setNominalViewingTransform();

        // Ensure at least 5 msec per frame (i.e., < 200Hz)
        universe.getViewer().getView().setMinimumFrameCycleTime(5);

        // make up an interesting wee scene
        scene = createSceneGraph();

        // add the scene to the Java3D universe so it can be traversed and rendered
        universe.addBranchGraph(scene);

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
            if (universe != null) {
                universe.cleanup();
                universe = null;
            }
            super.onDestroy();
        } catch (Exception e) {
            //ignore as we are done
            e.printStackTrace();
        }
    }


    public BranchGroup createSceneGraph() {
        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        // Create the TransformGroup node and initialize it to the identity. Enable the TRANSFORM_WRITE capability so that
        // our behavior code can modify it at run time. Add it to the root of the subgraph.
        TransformGroup objTrans = new TransformGroup();
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRoot.addChild(objTrans);

        // Create a simple Shape3D node; add it to the scene graph.
        objTrans.addChild(new Cube(0.4f));

        // Create a new Behavior object that will perform the desired operation on the specified transform and add
        // it into the scene graph.
        Transform3D yAxis = new Transform3D();
        Alpha rotationAlpha = new Alpha(-1, 4000);
        RotationInterpolator rotator = new RotationInterpolator(rotationAlpha, objTrans, yAxis, 0.0f, (float) Math.PI * 2.0f);
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
        rotator.setSchedulingBounds(bounds);
        objRoot.addChild(rotator);

        // Have Java 3D perform optimizations on this scene graph.
        objRoot.compile();

        return objRoot;
    }

}


