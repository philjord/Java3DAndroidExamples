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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jogamp.newt.opengl.GLWindow;

import org.jogamp.java3d.AlternateAppearance;
import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.BoundingLeaf;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.Bounds;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.examples.java3dhelloworld.R;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3f;

import jogamp.newt.driver.android.NewtBaseFragment;
import jogamp.newt.driver.android.NewtBaseFragmentActivity;

public class AlternateAppearanceBoundsTest extends NewtBaseFragmentActivity {


    Material mat1, altMat;
    SimpleShaderAppearance app, otherApp;
    Spinner altAppMaterialColor;
    Spinner appMaterialColor;
    CheckBox useBoundingLeaf;
    CheckBox override;
    Spinner boundsType;
    private Group content1 = null;
    AlternateAppearance altApp;
    Shape3D[] shapes1;
    boolean boundingLeafOn = false;
    // Globally used colors
    Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
    Color3f red = new Color3f(1.0f, 0.0f, 0.0f);
    Color3f green = new Color3f(0.0f, 1.0f, 0.0f);
    Color3f blue = new Color3f(0.0f, 0.0f, 1.0f);
    Color3f[] colors = {white, red, green, blue};

    private Bounds worldBounds = new BoundingSphere(
            new Point3d(0.0, 0.0, 0.0),  // Center
            1000.0);                      // Extent
    private Bounds smallBounds = new BoundingSphere(
            new Point3d(0.0, 0.0, 0.0),  // Center
            0.25);                         // Extent
    private Bounds tinyBounds = new BoundingSphere(
            new Point3d(0.0, 0.0, 0.0),  // Center
            0.05);                         // Extent
    private BoundingLeaf leafBounds = null;
    private int currentBounds = 2;

    private Bounds[] allBounds = {tinyBounds, smallBounds, worldBounds};

    DirectionalLight light1 = null;

    // Get the current bounding leaf position
    private int currentPosition = 0;
    //    Point3f pos = (Point3f)positions[currentPosition].value;

    private SimpleUniverse u = null;

    private Canvas3D c;

    @Override
    public void onCreate(final Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.altappearbounds);
        SimpleShaderAppearance.setVersionES300();

        c = new Canvas3D();

        BranchGroup scene = createSceneGraph();
        // SimpleUniverse is a Convenience Utility class
        u = new SimpleUniverse(c);

        // This will move the ViewPlatform back a bit so the
        // objects in the scene can be viewed.
        u.getViewingPlatform().setNominalViewingTransform();
        u.addBranchGraph(scene);


        // Create GUI
        String boundsValues[] = {"Tiny Bounds", "Small Bounds", "Big Bounds"};

        boundsType = findViewById(R.id.boundsType);
        boundsType.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, boundsValues)
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
                ret.setText((String)boundsType.getItemAtPosition(position));
                return ret;
            }
        });
        boundsType.setOnItemSelectedListener(listener);
        boundsType.setSelection(2);

        useBoundingLeaf = findViewById(R.id.useBoundingLeaf);
        useBoundingLeaf.setSelected(boundingLeafOn);
        useBoundingLeaf.setOnCheckedChangeListener(listener2);

        override = findViewById(R.id.override);
        override.setSelected(false);
        override.setOnCheckedChangeListener(listener2);


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
        NewtBaseFragment nbf = new NewtBaseFragment2(c.getGLWindow());
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


    @Override
    public void onPause() {
        // allow jogl to exit gracefully, not this does not allow resumes without more complex lifecycle code
        if (c != null) {
            c.stopRenderer();
            c.removeNotify();
            c.getGLWindow().destroy();
        }
        super.onPause();
    }

    BranchGroup createSceneGraph() {
        BranchGroup objRoot = new BranchGroup();

        // Create an alternate appearance
        otherApp = new SimpleShaderAppearance();
        altMat = new Material();
        altMat.setCapability(Material.ALLOW_COMPONENT_WRITE);
        altMat.setDiffuseColor(new Color3f(0.0f, 1.0f, 0.0f));
        otherApp.setMaterial(altMat);

        altApp = new AlternateAppearance();
        altApp.setAppearance(otherApp);
        altApp.setCapability(AlternateAppearance.ALLOW_BOUNDS_WRITE);
        altApp.setCapability(AlternateAppearance.ALLOW_INFLUENCING_BOUNDS_WRITE);
        altApp.setInfluencingBounds(worldBounds);
        objRoot.addChild(altApp);

        // Build foreground geometry
        app = new SimpleShaderAppearance();
        mat1 = new Material();
        mat1.setCapability(Material.ALLOW_COMPONENT_WRITE);
        mat1.setDiffuseColor(new Color3f(1.0f, 0.0f, 0.0f));
        app.setMaterial(mat1);
        content1 = new SphereGroup(
                0.05f,   // radius of spheres
                0.15f,    // x spacing
                0.15f,   // y spacing
                5,       // number of spheres in X
                5,       // number of spheres in Y
                app, // appearance
                true);  // alt app override = true
        objRoot.addChild(content1);
        shapes1 = ((SphereGroup) content1).getShapes();


        // Add lights
        light1 = new DirectionalLight();
        light1.setEnable(true);
        light1.setColor(new Color3f(0.2f, 0.2f, 0.2f));
        light1.setDirection(new Vector3f(1.0f, 0.0f, -1.0f));
        light1.setInfluencingBounds(worldBounds);
        light1.setCapability(
                DirectionalLight.ALLOW_INFLUENCING_BOUNDS_WRITE);
        light1.setCapability(
                DirectionalLight.ALLOW_BOUNDS_WRITE);
        objRoot.addChild(light1);

        // Add an ambient light to dimly illuminate the rest of
        // the shapes in the scene to help illustrate that the
        // directional lights are being scoped... otherwise it looks
        // like we're just removing shapes from the scene
        AmbientLight ambient = new AmbientLight();
        ambient.setEnable(true);
        ambient.setColor(new Color3f(1.0f, 1.0f, 1.0f));
        ambient.setInfluencingBounds(worldBounds);
        objRoot.addChild(ambient);


        // Define a bounding leaf
        leafBounds = new BoundingLeaf(allBounds[currentBounds]);
        leafBounds.setCapability(BoundingLeaf.ALLOW_REGION_WRITE);
        objRoot.addChild(leafBounds);
        if (boundingLeafOn) {
            altApp.setInfluencingBoundingLeaf(leafBounds);
        } else {
            altApp.setInfluencingBounds(allBounds[currentBounds]);
        }


        return objRoot;
    }


    AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
        public void onNothingSelected(AdapterView<?> parent) {  }//odd
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            actionPerformed(parent);
        }
    };
    CompoundButton.OnCheckedChangeListener listener2 = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            actionPerformed(buttonView);
        }
    };


    public void actionPerformed(View parent) {
        Object target = parent;
        if (target == altAppMaterialColor) {
            altMat.setDiffuseColor(colors[altAppMaterialColor.getSelectedItemPosition()]);
        } else if (target == useBoundingLeaf) {
            boundingLeafOn = useBoundingLeaf.isSelected();
            if (boundingLeafOn) {
                leafBounds.setRegion(allBounds[currentBounds]);
                altApp.setInfluencingBoundingLeaf(leafBounds);
            } else {
                altApp.setInfluencingBoundingLeaf(null);
                altApp.setInfluencingBounds(allBounds[currentBounds]);
            }
        } else if (target == boundsType) {
            currentBounds = boundsType.getSelectedItemPosition();
            if (boundingLeafOn) {
                leafBounds.setRegion(allBounds[currentBounds]);
                altApp.setInfluencingBoundingLeaf(leafBounds);
            } else {
                altApp.setInfluencingBoundingLeaf(null);
                altApp.setInfluencingBounds(allBounds[currentBounds]);
            }
        } else if (target == override) {
            for (int i = 0; i < shapes1.length; i++)
                shapes1[i].setAppearanceOverrideEnable(override.isSelected());
        } else if (target == appMaterialColor) {
            mat1.setDiffuseColor(colors[appMaterialColor.getSelectedItemPosition()]);
        }

    }
}			   
