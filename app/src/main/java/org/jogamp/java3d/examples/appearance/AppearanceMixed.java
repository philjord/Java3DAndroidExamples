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

package org.jogamp.java3d.examples.appearance;

import android.os.Bundle;

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.GraphicsContext3D;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.IndexedTriangleArray;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PointAttributes;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.TextureAttributes;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import javaawt.image.VMBufferedImage;
import javaawt.imageio.VMImageIO;
import jogamp.newt.driver.android.NewtBaseActivity;

public class AppearanceMixed extends NewtBaseActivity {

    private java.net.URL texImage = null;
    private java.net.URL bgImage = null;
    private SimpleUniverse univ = null;
    private BranchGroup scene = null;

    static class MyCanvas3D extends Canvas3D {
        private GraphicsContext3D gc;

        private static final int vertIndices[] = {0, 1, 2, 0, 2, 3};
        private static final int normalIndices[] = {0, 0, 0, 1, 1, 1};
        private IndexedTriangleArray tri =
                new IndexedTriangleArray(4, IndexedTriangleArray.COORDINATES |
                        IndexedTriangleArray.NORMALS, 6);

        private Point3f vert[] = {
                new Point3f(-0.12f, -0.12f, 0.0f),
                new Point3f(0.12f, -0.12f, 0.0f),
                new Point3f(0.12f, 0.12f, 0.0f),
                new Point3f(-0.12f, 0.12f, 0.0f),
        };

        private Point3f min[] = {
                new Point3f(-0.24f, -0.24f, -0.20f),
                new Point3f(0.04f, -0.28f, -0.24f),
                new Point3f(0.00f, 0.00f, -0.24f),
                new Point3f(-0.32f, 0.08f, -0.20f),
        };

        private Point3f max[] = {
                new Point3f(-0.04f, -0.04f, 0.12f),
                new Point3f(0.32f, -0.04f, 0.16f),
                new Point3f(0.36f, 0.28f, 0.20f),
                new Point3f(-0.04f, 0.24f, 0.16f),
        };

        private Point3f delta[] = {
                new Point3f(-0.0021f, -0.0017f, 0.0014f),
                new Point3f(0.0025f, -0.0013f, -0.0018f),
                new Point3f(0.0021f, 0.0017f, 0.0018f),
                new Point3f(-0.0025f, 0.0013f, -0.0014f),
        };

        private Vector3f normals[];
        private Vector3f v01 = new Vector3f();
        private Vector3f v02 = new Vector3f();
        private Vector3f v03 = new Vector3f();

        public void renderField(int fieldDesc) {
            computeVert();
            computeNormals();
            gc.draw(tri);
        }

        private void computeVert() {
            for (int i = 0; i < 4; i++) {
                vert[i].add(delta[i]);
                if (vert[i].x > max[i].x) {
                    vert[i].x = max[i].x;
                    delta[i].x *= -1.0f;
                }
                if (vert[i].x < min[i].x) {
                    vert[i].x = min[i].x;
                    delta[i].x *= -1.0f;
                }
                if (vert[i].y > max[i].y) {
                    vert[i].y = max[i].y;
                    delta[i].y *= -1.0f;
                }
                if (vert[i].y < min[i].y) {
                    vert[i].y = min[i].y;
                    delta[i].y *= -1.0f;
                }
                if (vert[i].z > max[i].z) {
                    vert[i].z = max[i].z;
                    delta[i].z *= -1.0f;
                }
                if (vert[i].z < min[i].z) {
                    vert[i].z = min[i].z;
                    delta[i].z *= -1.0f;
                }
            }
            tri.setCoordinates(0, vert);
        }

        private void computeNormals() {
            v01.sub(vert[1], vert[0]);
            v02.sub(vert[2], vert[0]);
            v03.sub(vert[3], vert[0]);
            normals[0].cross(v01, v02);
            normals[0].normalize();
            normals[1].cross(v02, v03);
            normals[1].normalize();
            tri.setNormals(0, normals);
        }

        public MyCanvas3D() {
            super();

            // Allocate memory for normals
            normals = new Vector3f[2];
            normals[0] = new Vector3f();
            normals[1] = new Vector3f();

            // Set up the indices
            tri.setCoordinateIndices(0, vertIndices);
            tri.setNormalIndices(0, normalIndices);

            // Set up the graphics context
            gc = getGraphicsContext3D();

            // Create the appearance for the triangle fan
            SimpleShaderAppearance app = new SimpleShaderAppearance();
            Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
            Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
            Color3f objColor = new Color3f(0.0f, 0.0f, 0.8f);
            app.setMaterial(new Material(objColor, black, objColor,
                    white, 80.0f));
            gc.setAppearance(app);

            // Set up the global lights
            Color3f lColor1 = new Color3f(0.7f, 0.7f, 0.7f);
            Vector3f lDir1 = new Vector3f(-1.0f, -1.0f, -1.0f);
            Color3f alColor = new Color3f(0.2f, 0.2f, 0.2f);
            gc.addLight(new AmbientLight(alColor));
            gc.addLight(new DirectionalLight(lColor1, lDir1));
        }
    }


    private BranchGroup createSceneGraph() {
        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        // Create a bounds for the background and lights
        BoundingSphere bounds =
                new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

        // Set up the background
        TextureLoader bgTexture = new TextureLoader(bgImage, this);
        Background bg = new Background(bgTexture.getImage());
        bg.setApplicationBounds(bounds);
        objRoot.addChild(bg);

        // Set up the global lights
        Color3f lColor1 = new Color3f(0.7f, 0.7f, 0.7f);
        Vector3f lDir1 = new Vector3f(-1.0f, -1.0f, -1.0f);
        Color3f alColor = new Color3f(0.2f, 0.2f, 0.2f);

        AmbientLight aLgt = new AmbientLight(alColor);
        aLgt.setInfluencingBounds(bounds);
        DirectionalLight lgt1 = new DirectionalLight(lColor1, lDir1);
        lgt1.setInfluencingBounds(bounds);
        objRoot.addChild(aLgt);
        objRoot.addChild(lgt1);

        // Create a bunch of objects with a behavior and add them
        // into the scene graph.

        int row, col;
        SimpleShaderAppearance[][] app = new SimpleShaderAppearance[3][3];

        for (row = 0; row < 3; row++)
            for (col = 0; col < 3; col++)
                app[row][col] = createAppearance(row * 3 + col);

        for (int i = 0; i < 3; i++) {
            double ypos = (double) (i - 1) * 0.6;
            for (int j = 0; j < 3; j++) {
                double xpos = (double) (j - 1) * 0.6;
                objRoot.addChild(createObject(app[i][j], 0.12, xpos, ypos));
            }
        }

        // Let Java 3D perform optimizations on this scene graph.
        objRoot.compile();

        return objRoot;
    }


    private SimpleShaderAppearance createAppearance(int idx) {
        SimpleShaderAppearance app = new SimpleShaderAppearance();

        // Globally used colors
        Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
        Color3f white = new Color3f(1.0f, 1.0f, 1.0f);

        switch (idx) {
            // Unlit solid
            case 0: {
                // Set up the coloring properties
                Color3f objColor = new Color3f(1.0f, 0.2f, 0.4f);
                ColoringAttributes ca = new ColoringAttributes();
                ca.setColor(objColor);
                app.setColoringAttributes(ca);
                break;
            }


            // Unlit wire frame
            case 1: {
                // Set up the coloring properties
                Color3f objColor = new Color3f(0.5f, 0.0f, 0.2f);
                ColoringAttributes ca = new ColoringAttributes();
                ca.setColor(objColor);
                app.setColoringAttributes(ca);

                // Set up the polygon attributes
                PolygonAttributes pa = new PolygonAttributes();
                pa.setPolygonMode(pa.POLYGON_LINE);
                pa.setCullFace(pa.CULL_NONE);
                app.setPolygonAttributes(pa);
                break;
            }

            // Unlit points
            case 2: {
                // Set up the coloring properties
                Color3f objColor = new Color3f(0.2f, 0.2f, 1.0f);
                ColoringAttributes ca = new ColoringAttributes();
                ca.setColor(objColor);
                app.setColoringAttributes(ca);

                // Set up the polygon attributes
                PolygonAttributes pa = new PolygonAttributes();
                pa.setPolygonMode(pa.POLYGON_POINT);
                pa.setCullFace(pa.CULL_NONE);
                app.setPolygonAttributes(pa);

                // Set up point attributes
                PointAttributes pta = new PointAttributes();
                pta.setPointSize(5.0f);
                app.setPointAttributes(pta);
                break;
            }

            // Lit solid
            case 3: {
                // Set up the material properties
                Color3f objColor = new Color3f(0.8f, 0.0f, 0.0f);
                app.setMaterial(new Material(objColor, black, objColor,
                        white, 80.0f));
                break;
            }

            // Texture mapped, lit solid
            case 4: {
                // Set up the texture map
                TextureLoader tex = new TextureLoader(texImage, this);
                app.setTexture(tex.getTexture());

                TextureAttributes texAttr = new TextureAttributes();
                texAttr.setTextureMode(TextureAttributes.MODULATE);
                app.setTextureAttributes(texAttr);


                // Set up the material properties
                app.setMaterial(new Material(white, black, white, black, 1.0f));
                break;
            }

            // Transparent, lit solid
            case 5: {
                // Set up the transparency properties
                TransparencyAttributes ta = new TransparencyAttributes();
                ta.setTransparencyMode(ta.BLENDED);
                ta.setTransparency(0.6f);
                app.setTransparencyAttributes(ta);

                // Set up the polygon attributes
                PolygonAttributes pa = new PolygonAttributes();
                pa.setCullFace(pa.CULL_NONE);
                app.setPolygonAttributes(pa);

                // Set up the material properties
                Color3f objColor = new Color3f(0.7f, 0.8f, 1.0f);
                app.setMaterial(new Material(objColor, black, objColor,
                        black, 1.0f));
                break;
            }

            // Lit solid, no specular
            case 6: {
                // Set up the material properties
                Color3f objColor = new Color3f(0.8f, 0.0f, 0.0f);
                app.setMaterial(new Material(objColor, black, objColor,
                        black, 80.0f));
                break;
            }

            // Lit solid, specular only
            case 7: {
                // Set up the material properties
                Color3f objColor = new Color3f(0.8f, 0.0f, 0.0f);
                app.setMaterial(new Material(black, black, black,
                        white, 80.0f));
                break;
            }

            // Another lit solid with a different color
            case 8: {
                // Set up the material properties
                Color3f objColor = new Color3f(0.8f, 0.8f, 0.0f);
                app.setMaterial(new Material(objColor, black, objColor,
                        white, 80.0f));
                break;
            }

            default: {
                ColoringAttributes ca = new ColoringAttributes();
                ca.setColor(new Color3f(0.0f, 1.0f, 0.0f));
                app.setColoringAttributes(ca);
            }
        }

        return app;
    }


    private Group createObject(SimpleShaderAppearance app, double scale,
                               double xpos, double ypos) {

        // Create a transform group node to scale and position the object.
        Transform3D t = new Transform3D();
        t.set(scale, new Vector3d(xpos, ypos, 0.0));
        TransformGroup objTrans = new TransformGroup(t);

        // Create a second transform group node and initialize it to the
        // identity.  Enable the TRANSFORM_WRITE capability so that
        // our behavior code can modify it at runtime.
        TransformGroup spinTg = new TransformGroup();
        spinTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        // Create a simple shape leaf node and set the appearance
        Shape3D shape = new Tetrahedron(app);


        // add it to the scene graph.
        spinTg.addChild(shape);

        // Create a new Behavior object that will perform the desired
        // operation on the specified transform object and add it into
        // the scene graph.
        Transform3D yAxis = new Transform3D();
        Alpha rotationAlpha = new Alpha(-1, Alpha.INCREASING_ENABLE,
                0, 0,
                5000, 0, 0,
                0, 0, 0);

        RotationInterpolator rotator =
                new RotationInterpolator(rotationAlpha, spinTg, yAxis,
                        0.0f, (float) Math.PI * 2.0f);

        BoundingSphere bounds =
                new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

        rotator.setSchedulingBounds(bounds);

        // Add the behavior and the transform group to the object
        objTrans.addChild(rotator);
        objTrans.addChild(spinTg);

        return objTrans;
    }


    private Canvas3D createUniverse() {

        // Create a MyCanvas3D
        MyCanvas3D c = new MyCanvas3D();

        // Create simple universe with view branch
        univ = new SimpleUniverse(c);

        // This will move the ViewPlatform back a bit so the
        // objects in the scene can be viewed.
        univ.getViewingPlatform().setNominalViewingTransform();

        // Ensure at least 5 msec per frame (i.e., < 200Hz)
        univ.getViewer().getView().setMinimumFrameCycleTime(5);

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

        if (bgImage == null) {
            // the path to the image for an applet
            bgImage = getClass().getResource("/resources/images/bg.jpg");
            if (bgImage == null) {
                System.err.println("/resources/images/bg.jpg not found");
                System.exit(1);
            }
        }

        if (texImage == null) {
            // the path to the image for an applet
            texImage = getClass().getResource("/resources/images/stone.jpg");
            if (texImage == null) {
                System.err.println("/resources/images/stone.jpg not found");
                System.exit(1);
            }
        }

        // Create Canvas3D and SimpleUniverse; add canvas to drawing panel
        c = createUniverse();
        //c.addNotify();//if Canvas3D is created with no capabilities handed in, no addNotify required

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
