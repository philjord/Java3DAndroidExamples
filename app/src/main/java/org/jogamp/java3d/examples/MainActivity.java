package org.jogamp.java3d.examples;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jogamp.java3d.examples.alternate_appearance.AlternateAppearanceBoundsTest;
import org.jogamp.java3d.examples.appearance.AppearanceMixed;
import org.jogamp.java3d.examples.appearance.AppearanceTest;
import org.jogamp.java3d.examples.background.BackgroundGeometry;
import org.jogamp.java3d.examples.background.BackgroundTexture;
import org.jogamp.java3d.examples.collision.TickTockCollision;
import org.jogamp.java3d.examples.depth_func.DepthFuncTest;
import org.jogamp.java3d.examples.distort_glyph.DistortGlyphTest;
import org.jogamp.java3d.examples.fps_counter.FPSCounterDemo;
import org.jogamp.java3d.examples.gears.GearBox;
import org.jogamp.java3d.examples.gears.GearTest;
import org.jogamp.java3d.examples.gl2es2pipeline.EnvironmentMappingGLSL;
import org.jogamp.java3d.examples.gl2es2pipeline.ObjLoadGLSL;
import org.jogamp.java3d.examples.gl2es2pipeline.PhongShadingGLSL;
import org.jogamp.java3d.examples.gl2es2pipeline.SamplerTestGLSL;
import org.jogamp.java3d.examples.gl2es2pipeline.ShaderTestGLSL;
import org.jogamp.java3d.examples.gl2es2pipeline.SphereGLSL;
import org.jogamp.java3d.examples.gl2es2pipeline.VertexAttrTestGLSL;
import org.jogamp.java3d.examples.hello_universe.HelloUniverseActivity;
import org.jogamp.java3d.examples.java3dexamples.R;
import org.jogamp.java3d.examples.lightwave.LightWaveViewer;
import org.jogamp.java3d.examples.lod.LOD;
import org.jogamp.java3d.examples.objload.ObjLoad;
import org.jogamp.java3d.examples.platform_geometry.SimpleGeometry;
import org.jogamp.java3d.examples.pure_immediate.PureImmediate;
import org.jogamp.java3d.examples.pure_immediate.PureImmediateStereo;
import org.jogamp.java3d.examples.raster.RasterTest;
import org.jogamp.java3d.examples.raster.ReadRaster;
import org.jogamp.java3d.examples.sphere_motion.SphereMotion;
import org.jogamp.java3d.examples.sphere_motion.SphereMotionGL2ES2_Texture;
import org.jogamp.java3d.examples.spline_anim.SplineAnim;
import org.jogamp.java3d.examples.stencil.StencilOutline;

import java.io.OutputStream;
import java.io.PrintStream;


public class MainActivity extends Activity {

    @Override
    public void onCreate(final Bundle state) {
        super.onCreate(state);

        // get system out to log
        PrintStream interceptor = new SopInterceptor(System.out, "sysout");
        System.setOut(interceptor);
        PrintStream interceptor2 = new SopInterceptor(System.err, "syserr");
        System.setErr(interceptor2);

        setContentView(R.layout.main);
        configureList();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_test_3d:
                //test3d();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // deal with any file access request etc
    }


    public static final String[] configNames = new String[]
            {
                    "Hello World",
                    "Multi Texture",
            };

    private void configureList() {


        String[] exampleNames = {
                "AlternateAppearanceBoundsTest", "AlternateAppearanceScopeTest",
                "AppearanceMixed", "AppearanceTest",
                "BackgroundTexture", "BackgroundGeometry",
                "TickTockCollision",
                "DepthFuncTest",
                "DistortGlyphTest",
                "FPSCounterDemo",
                "GearBox", "GearTest",
                "EnvironmentMappingGLSL",  "ObjLoadGLSL","PhongShadingGLSL","SamplerTestGLSL","ShaderTestGLSL","SphereGLSL","VertexAttrTestGLSL",
                "HelloUniverseActivity",
                //not working "LightWaveViewer",
                "LOD",
                "ObjLoad",
                "SimpleGeometry",
                //not working "PureImmediate","PureImmediateStereo",
                "RasterTest",//not working  "ReadRaster",
                "SphereMotion","SphereMotionGL2ES2_Texture",
                "SplineAnim",
                "StencilOutline",
                // not appropriate for a touch device "VirtualInputDeviceTest"
        };
        final ListView exampleList = findViewById(R.id.examplelist);

        exampleList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, exampleNames) {
            @Override
            public View getView(int pos, View view, ViewGroup parent) {
                view = super.getView(pos, view, parent);
                ((TextView) view).setSingleLine(true);
                return view;
            }
        });


        exampleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                // send the which through and hope they match up
                String selectedItem = parent.getItemAtPosition(which).toString();  // how to avoid a cast and all that goes with it. (lazyness)
                if ("AlternateAppearanceBoundsTest".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, AlternateAppearanceBoundsTest.class);
                    startActivity(intent);
                } else if ("AlternateAppearanceScopeTest".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, AlternateAppearanceBoundsTest.class);
                    startActivity(intent);
                } else if ("AppearanceMixed".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, AppearanceMixed.class);
                    startActivity(intent);
                } else if ("AppearanceTest".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, AppearanceTest.class);
                    startActivity(intent);
                } else if ("BackgroundGeometry".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, BackgroundGeometry.class);
                    startActivity(intent);
                } else if ("BackgroundTexture".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, BackgroundTexture.class);
                    startActivity(intent);
                } else if ("TickTockCollision".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, TickTockCollision.class);
                    startActivity(intent);
                } else if ("DepthFuncTest".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, DepthFuncTest.class);
                    startActivity(intent);
                } else if ("DistortGlyphTest".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, DistortGlyphTest.class);
                    startActivity(intent);
                } else if ("FPSCounterDemo".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, FPSCounterDemo.class);
                    startActivity(intent);
                } else if ("GearBox".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, GearBox.class);
                    startActivity(intent);
                } else if ("GearTest".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, GearTest.class);
                    startActivity(intent);
                } else if ("EnvironmentMappingGLSL".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, EnvironmentMappingGLSL.class);
                    startActivity(intent);
                } else if ("ObjLoadGLSL".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, ObjLoadGLSL.class);
                    startActivity(intent);
                } else if ("PhongShadingGLSL".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, PhongShadingGLSL.class);
                    startActivity(intent);
                } else if ("SamplerTestGLSL".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, SamplerTestGLSL.class);
                    startActivity(intent);
                } else if ("ShaderTestGLSL".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, ShaderTestGLSL.class);
                    startActivity(intent);
                } else if ("SphereGLSL".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, SphereGLSL.class);
                    startActivity(intent);
                } else if ("VertexAttrTestGLSL".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, VertexAttrTestGLSL.class);
                    startActivity(intent);
                } else if ("HelloUniverseActivity".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, HelloUniverseActivity.class);
                    startActivity(intent);
                } else if ("LightWaveViewer".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, LightWaveViewer.class);
                    startActivity(intent);
                } else if ("LOD".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, LOD.class);
                    startActivity(intent);
                } else if ("ObjLoad".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, ObjLoad.class);
                    startActivity(intent);
                } else if ("SimpleGeometry".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, SimpleGeometry.class);
                    startActivity(intent);
                } else if ("PureImmediate".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, PureImmediate.class);
                    startActivity(intent);
                } else if ("PureImmediateStereo".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, PureImmediateStereo.class);
                    startActivity(intent);
                } else if ("RasterTest".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, RasterTest.class);
                    startActivity(intent);
                } else if ("ReadRaster".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, ReadRaster.class);
                    startActivity(intent);
                } else if ("SphereMotion".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, SphereMotion.class);
                    startActivity(intent);
                } else if ("SphereMotionGL2ES2_Texture".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, SphereMotionGL2ES2_Texture.class);
                    startActivity(intent);
                } else if ("SplineAnim".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, SplineAnim.class);
                    startActivity(intent);
                } else if ("StencilOutline".equals(selectedItem)) {
                    Intent intent = new Intent(MainActivity.this, StencilOutline.class);
                    startActivity(intent);
                }



            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }



    /**
     * Redirect System.out or System.err to logcat
     */
    public class SopInterceptor extends PrintStream {
        private String tag;

        public SopInterceptor(OutputStream out, String tag) {
            super(out, true);
            this.tag = tag;
        }

        @Override
        public void print(String s) {
            Log.w(tag, s);
        }
    }
}
