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

package org.jogamp.java3d.examples.objload;

import android.os.Bundle;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.loaders.IncorrectFormatException;
import org.jogamp.java3d.loaders.ParsingErrorException;
import org.jogamp.java3d.loaders.Scene;
import org.jogamp.java3d.loaders.objectfile.ObjectFile;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.PlatformGeometry;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3f;

import jogamp.newt.driver.android.NewtBaseActivity;

/**
 * Simple Java 3D example program to display an .obj object.
 */
public class ObjLoad extends NewtBaseActivity {

    private boolean spin = false;
    private boolean noTriangulate = false;
    private boolean noStripify = false;
    private double creaseAngle = 60.0;
    private URL filename = null;

    private SimpleUniverse univ = null;
    private BranchGroup scene = null;

    public BranchGroup createSceneGraph() {
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

        int flags = ObjectFile.RESIZE;
        if (!noTriangulate) flags |= ObjectFile.TRIANGULATE;
        if (!noStripify) flags |= ObjectFile.STRIPIFY;
        ObjectFile f = new ObjectFile(flags,
          (float)(creaseAngle * Math.PI / 180.0));
        Scene s = null;
        try {
          s = f.load(filename);
        }
        catch (FileNotFoundException e) {
          System.err.println(e);
          System.exit(1);
        }
        catch (ParsingErrorException e) {
          System.err.println(e);
          System.exit(1);
        }
        catch (IncorrectFormatException e) {
          System.err.println(e);
          System.exit(1);
        }

        objTrans.addChild(s.getSceneGroup());

        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

            if (spin) {
          Transform3D yAxis = new Transform3D();
          Alpha rotationAlpha = new Alpha(-1, Alpha.INCREASING_ENABLE,
                          0, 0,
                          4000, 0, 0,
                          0, 0, 0);

          RotationInterpolator rotator =
              new RotationInterpolator(rotationAlpha, objTrans, yAxis,
                           0.0f, (float) Math.PI*2.0f);
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
    
    private Canvas3D createUniverse() {
        // Create a Canvas3D using the preferred configuration
        Canvas3D canvas3d = new Canvas3D();

        // Create simple universe with view branch
        univ = new SimpleUniverse(canvas3d);
            BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

        // add mouse behaviors to the ViewingPlatform
        ViewingPlatform viewingPlatform = univ.getViewingPlatform();

        PlatformGeometry pg = new PlatformGeometry();

        // Set up the ambient light
        Color3f ambientColor = new Color3f(0.1f, 0.1f, 0.1f);
        AmbientLight ambientLightNode = new AmbientLight(ambientColor);
        ambientLightNode.setInfluencingBounds(bounds);
        pg.addChild(ambientLightNode);

        // Set up the directional lights
        Color3f light1Color = new Color3f(1.0f, 1.0f, 0.9f);
        Vector3f light1Direction  = new Vector3f(1.0f, 1.0f, 1.0f);
        Color3f light2Color = new Color3f(1.0f, 1.0f, 1.0f);
        Vector3f light2Direction  = new Vector3f(-1.0f, -1.0f, -1.0f);

        DirectionalLight light1
            = new DirectionalLight(light1Color, light1Direction);
        light1.setInfluencingBounds(bounds);
        pg.addChild(light1);

        DirectionalLight light2
            = new DirectionalLight(light2Color, light2Direction);
        light2.setInfluencingBounds(bounds);
        pg.addChild(light2);

        viewingPlatform.setPlatformGeometry( pg );

        // This will move the ViewPlatform back a bit so the
        // objects in the scene can be viewed.
        viewingPlatform.setNominalViewingTransform();

        /*if (!spin) {
                OrbitBehavior orbit = new OrbitBehavior(canvas3d,
                                OrbitBehavior.REVERSE_ALL);
                orbit.setSchedulingBounds(bounds);
                viewingPlatform.setViewPlatformBehavior(orbit);
        }  */

        // Ensure at least 5 msec per frame (i.e., < 200Hz)
        univ.getViewer().getView().setMinimumFrameCycleTime(5);

        return canvas3d;
    }

    private void usage() {
        System.out.println(
                "Usage: java ObjLoad [-s] [-n] [-t] [-c degrees] <.obj file>");
        System.out.println("  -s Spin (no user interaction)");
        System.out.println("  -n No triangulation");
        System.out.println("  -t No stripification");
        System.out.println(
                "  -c Set crease angle for normal generation (default is 60 without");
        System.out.println(
                "     smoothing group info, otherwise 180 within smoothing groups)");
        System.exit(0);
    }

    // ----------------------------------------------------------------

    private Canvas3D c = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SimpleShaderAppearance.setVersionES300();

        // just make it spin
        String[] args = new String[]{"-s"};
        if (args.length != 0) {
            for (int i = 0 ; i < args.length ; i++) {
                if (args[i].startsWith("-")) {
                    if (args[i].equals("-s")) {
                        spin = true;
                    } else if (args[i].equals("-n")) {
                        noTriangulate = true;
                    } else if (args[i].equals("-t")) {
                        noStripify = true;
                    } else if (args[i].equals("-c")) {
                        if (i < args.length - 1) {
                            creaseAngle = (new Double(args[++i])).doubleValue();
                        } else usage();
                    } else {
                        usage();
                    }
                } else {
                    try {
                        if ((args[i].indexOf("file:") == 0) ||
                                (args[i].indexOf("http") == 0)) {
                            filename = new URL(args[i]);
                        } else if (args[i].charAt(0) != '/') {
                            filename = new URL("file:./" + args[i]);
                        } else {
                            filename = new URL("file:" + args[i]);
                        }
                    } catch (MalformedURLException e) {
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

        // Create Canvas3D and SimpleUniverse; add canvas to drawing panel
        c = createUniverse();

        c.getGLWindow().getDelegatedWindow().setSize(700, 700);

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
}
