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

package org.jogamp.java3d.examples.alternate_appearance;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jogamp.newt.opengl.GLWindow;

import org.jogamp.java3d.AlternateAppearance;
import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.examples.java3dexamples.R;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3f;

import jogamp.newt.driver.android.NewtBaseFragment;
import jogamp.newt.driver.android.NewtBaseFragmentActivity;

public class AlternateAppearanceScopeTest extends NewtBaseFragmentActivity {

    Material mat1, altMat;
    SimpleShaderAppearance app, otherApp;
    Spinner altAppMaterialColor;
    Spinner appMaterialColor;
    Spinner altAppScoping;
    Spinner override;
    private Group content1 = null;
    private Group content2 = null;
    BoundingSphere worldBounds;
    AlternateAppearance altApp;
    Shape3D[] shapes1, shapes2;
    boolean shape1Enabled = false, shape2Enabled = false;
    // Globally used colors
    Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
    Color3f red = new Color3f(1.0f, 0.0f, 0.0f);
    Color3f green = new Color3f(0.0f, 1.0f, 0.0f);
    Color3f blue = new Color3f(0.0f, 0.0f, 1.0f);
    Color3f[] colors = {white, red, green, blue};

    private SimpleUniverse u;

    private Canvas3D c;

    @Override
    public void onCreate(final Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.altappearscope);
        SimpleShaderAppearance.setVersionES300();

        c = new Canvas3D();

        BranchGroup scene = createSceneGraph();
        // SimpleUniverse is a Convenience Utility class
        u = new SimpleUniverse(c);

        // add mouse behaviors to the viewingPlatform
        ViewingPlatform viewingPlatform = u.getViewingPlatform();

        // This will move the ViewPlatform back a bit so the
        // objects in the scene can be viewed.
        viewingPlatform.setNominalViewingTransform();

        //OrbitBehavior orbit = new OrbitBehavior(c,
        //					OrbitBehavior.REVERSE_ALL);
        //BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
        //					   100.0);
        //orbit.setSchedulingBounds(bounds);
        //viewingPlatform.setViewPlatformBehavior(orbit);

        u.addBranchGraph(scene);


        String values[] = {"Scoped Set1", "Scoped Set2", "Universal Scope"};
        altAppScoping = findViewById(R.id.altAppScoping);
        altAppScoping.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                return getDropDownView(position, convertView, parent);
            }
            @Override
            public View getDropDownView (int position, View convertView, ViewGroup parent)
            {
                TextView ret = new TextView(getContext());
                ret.setText((String)altAppScoping.getItemAtPosition(position));
                return ret;
            }
        });
        altAppScoping.setOnItemSelectedListener(listener);
        altAppScoping.setSelection(2);

        String enables[] = {"Enabled Set1", "Enabled Set2", "Enabled set1&2", "Disabled set1&2"};

        override = findViewById(R.id.override);
        override.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, enables)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                return getDropDownView(position, convertView, parent);
            }
            @Override
            public View getDropDownView (int position, View convertView, ViewGroup parent)
            {
                TextView ret = new TextView(getContext());
                ret.setText((String)override.getItemAtPosition(position));
                return ret;
            }
        });
        override.setOnItemSelectedListener(listener);
        override.setSelection(3);

        String colorVals[] = {"WHITE", "RED", "GREEN", "BLUE"};

        altAppMaterialColor = findViewById(R.id.altAppMaterialColor);
        altAppMaterialColor.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, colorVals)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                return getDropDownView(position, convertView, parent);
            }
            @Override
            public View getDropDownView (int position, View convertView, ViewGroup parent)
            {
                TextView ret = new TextView(getContext());
                ret.setText((String)altAppMaterialColor.getItemAtPosition(position));
                return ret;
            }
        });
        altAppMaterialColor.setOnItemSelectedListener(listener);
        altAppMaterialColor.setSelection(2);

        appMaterialColor = findViewById(R.id.appMaterialColor);
        appMaterialColor.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, colorVals)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                return getDropDownView(position, convertView, parent);
            }
            @Override
            public View getDropDownView (int position, View convertView, ViewGroup parent)
            {
                TextView ret = new TextView(getContext());
                ret.setText((String)appMaterialColor.getItemAtPosition(position));
                return ret;
            }
        });
        appMaterialColor.setOnItemSelectedListener(listener);
        appMaterialColor.setSelection(1);

        // Android Fragments are a pain...
        NewtBaseFragment nbf = new AlternateAppearanceBoundsTest.NewtBaseFragment2(c.getGLWindow());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.java3dSpace, nbf);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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


    BranchGroup createSceneGraph() {
        BranchGroup objRoot = new BranchGroup();

        // Create influencing bounds
        worldBounds = new BoundingSphere(
                new Point3d(0.0, 0.0, 0.0),  // Center
                1000.0);                      // Extent

        Transform3D t = new Transform3D();
        // move the object upwards
        t.set(new Vector3f(0.0f, 0.1f, 0.0f));
        // Shrink the object
        t.setScale(0.8);

        TransformGroup trans = new TransformGroup(t);
        trans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        trans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);


        otherApp = new SimpleShaderAppearance();
        altMat = new Material();
        altMat.setCapability(Material.ALLOW_COMPONENT_WRITE);
        altMat.setDiffuseColor(new Color3f(0.0f, 1.0f, 0.0f));
        otherApp.setMaterial(altMat);

        altApp = new AlternateAppearance();
        altApp.setAppearance(otherApp);
        altApp.setCapability(AlternateAppearance.ALLOW_SCOPE_WRITE);
        altApp.setCapability(AlternateAppearance.ALLOW_SCOPE_READ);
        altApp.setInfluencingBounds(worldBounds);
        objRoot.addChild(altApp);

        // Build foreground geometry into two groups.  We'll
        // create three directional lights below, one each with
        // scope to cover the first geometry group only, the
        // second geometry group only, or both geometry groups.
        app = new SimpleShaderAppearance();
        mat1 = new Material();
        mat1.setCapability(Material.ALLOW_COMPONENT_WRITE);
        mat1.setDiffuseColor(new Color3f(1.0f, 0.0f, 0.0f));
        app.setMaterial(mat1);
        content1 = new SphereGroup(
                0.05f,   // radius of spheres
                0.4f,    // x spacing
                0.2f,   // y spacing
                3,       // number of spheres in X
                5,       // number of spheres in Y
                app, // appearance
                true);  // alt app override = true
        trans.addChild(content1);
        shapes1 = ((SphereGroup) content1).getShapes();

        content2 = new SphereGroup(
                0.05f,   // radius of spheres
                .4f,    // x spacing
                0.2f,   // y spacing
                2,       // number of spheres in X
                5,       // number of spheres in Y
                app,   // appearance
                true); // alt app override = true
        trans.addChild(content2);
        shapes2 = ((SphereGroup) content2).getShapes();


        // Add lights
        DirectionalLight light1 = null;
        light1 = new DirectionalLight();
        light1.setEnable(true);
        light1.setColor(new Color3f(0.2f, 0.2f, 0.2f));
        light1.setDirection(new Vector3f(1.0f, 0.0f, -1.0f));
        light1.setInfluencingBounds(worldBounds);
        objRoot.addChild(light1);

        DirectionalLight light2 = new DirectionalLight();
        light2.setEnable(true);
        light2.setColor(new Color3f(0.2f, 0.2f, 0.2f));
        light2.setDirection(new Vector3f(-1.0f, 0.0f, 1.0f));
        light2.setInfluencingBounds(worldBounds);
        objRoot.addChild(light2);

        // Add an ambient light to dimly illuminate the rest of
        // the shapes in the scene to help illustrate that the
        // directional lights are being scoped... otherwise it looks
        // like we're just removing shapes from the scene
        AmbientLight ambient = new AmbientLight();
        ambient.setEnable(true);
        ambient.setColor(new Color3f(1.0f, 1.0f, 1.0f));
        ambient.setInfluencingBounds(worldBounds);
        objRoot.addChild(ambient);

        objRoot.addChild(trans);

        return objRoot;
    }


    AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
        public void onNothingSelected(AdapterView<?> parent) {  }//odd
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            actionPerformed(parent);
        }
    };

    public void actionPerformed(View parent) {
        Object target = parent;
        if (target == altAppMaterialColor) {
            altMat.setDiffuseColor(colors[altAppMaterialColor.getSelectedItemPosition()]);
        } else if (target == altAppScoping) {
            for (int i = 0; i < altApp.numScopes(); i++) {
                altApp.removeScope(0);
            }
            if (altAppScoping.getSelectedItemPosition() == 0) {
                altApp.addScope(content1);
            } else if (altAppScoping.getSelectedItemPosition() == 1) {
                altApp.addScope(content2);
            }
        } else if (target == override) {
            int i;
            if (override.getSelectedItemPosition() == 0) {
                if (!shape1Enabled) {
                    for (i = 0; i < shapes1.length; i++)
                        shapes1[i].setAppearanceOverrideEnable(true);
                    shape1Enabled = true;
                }

                if (shape2Enabled) {
                    for (i = 0; i < shapes2.length; i++)
                        shapes2[i].setAppearanceOverrideEnable(false);
                    shape2Enabled = false;
                }
            } else if (override.getSelectedItemPosition() == 1) {
                if (!shape2Enabled) {
                    for (i = 0; i < shapes2.length; i++)
                        shapes2[i].setAppearanceOverrideEnable(true);
                    shape2Enabled = true;
                }

                if (shape1Enabled) {
                    for (i = 0; i < shapes1.length; i++)
                        shapes1[i].setAppearanceOverrideEnable(false);
                    shape1Enabled = false;
                }
            } else if (override.getSelectedItemPosition() == 2) {
                if (!shape1Enabled) {
                    for (i = 0; i < shapes1.length; i++)
                        shapes1[i].setAppearanceOverrideEnable(true);
                    shape1Enabled = true;
                }
                if (!shape2Enabled) {
                    for (i = 0; i < shapes2.length; i++)
                        shapes2[i].setAppearanceOverrideEnable(true);
                    shape2Enabled = true;
                }
            } else {
                if (shape1Enabled) {
                    for (i = 0; i < shapes1.length; i++)
                        shapes1[i].setAppearanceOverrideEnable(false);
                    shape1Enabled = false;
                }

                if (shape2Enabled) {
                    for (i = 0; i < shapes2.length; i++)
                        shapes2[i].setAppearanceOverrideEnable(false);
                    shape2Enabled = false;
                }
            }

        } else if (target == appMaterialColor) {
            mat1.setDiffuseColor(colors[appMaterialColor.getSelectedItemPosition()]);
        }

    }

}			   
