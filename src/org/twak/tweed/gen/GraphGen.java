package org.twak.tweed.gen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.vecmath.Point3d;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.lwjgl.Sys;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.twak.siteplan.jme.Jme3z;
import org.twak.tweed.EventMoveHandle;
import org.twak.tweed.Tweed;
import org.twak.tweed.TweedSettings;
import org.twak.utils.Filez;
import org.twak.utils.collections.Arrayz;
import org.twak.utils.geom.Graph3D;
import org.twak.utils.geom.Junction;
import org.twak.utils.geom.Street;
import org.twak.viewTrace.GMLReader;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;

public class GraphGen extends Gen implements ICanSave {

	public File source;

	public Set<Junction> jns;

	public GraphGen() {}
	public GraphGen( File source, Tweed tweed ) {
		super( Filez.stripExtn( source.getName( )), tweed);
		this.source = source;

		CoordinateReferenceSystem crss = Tweed.kludgeCMS.get( TweedSettings.settings.gmlCoordSystem );
		Graph3D graph = GMLReader.readGMLGraph( Tweed.toWorkspace( source ), DefaultGeocentricCRS.CARTESIAN, crss );
		
		graph.transform ( TweedSettings.settings.toOrigin );

//		Graph3D myGraph = new Graph3D();
//		myGraph.put( new Point3d(0,0,0), new Point3d(100, 0, 0));
//		myGraph.put( new Point3d(0,0,0), new Point3d(0, 0, 100));
//		myGraph.put( new Point3d(0,0,0), new Point3d(-150, 0, 0));

//		myGraph.put( new Point3d(100, 0, 0), new Point3d(0,0,0));
//		myGraph.put( new Point3d(0, 0, 100), new Point3d(0,0,0));
//		myGraph.put( new Point3d(-150, 0, 0), new Point3d(0,0,0));

//		myGraph.put( new Point3d(100, 0, 0), new Point3d(100,0,50));
//		myGraph.put( new Point3d(100, 0, 0), new Point3d(100,0,-40));
//
//		graph = myGraph;

		jns = graph.getAllDiscrete();

	}
	
	@Override
	public void calculate() {

		super.calculate();

		for ( Spatial s : gNode.getChildren() )
			s.removeFromParent();

		Random randy = new Random();
		
		Material lineMat = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );
		lineMat.setColor( "Diffuse", Jme3z.toJme( color ) );
		lineMat.setColor( "Ambient", Jme3z.toJme( color ) );
		lineMat.setBoolean( "UseMaterialColors", true );
		lineMat.getAdditionalRenderState().setLineWidth( 5f );

		for ( Junction p1 : jns ) {

			Box box1 = new Box( 2f, 2f, 2f );
			Geometry geom = new Geometry( "Box", box1 );

			geom.setUserData( EventMoveHandle.class.getSimpleName(), new Object[] { new EventMoveHandle() {
				@Override
				public void posChanged() {

//					List<Point3d> to = graph.get( p1 );
//					graph.remove( p1 );
					p1.set( Jme3z.from( geom.getLocalTranslation() ) );
//					graph.putAll( p1, to );
					calculate();
				}
			} } );

			ColorRGBA col = new ColorRGBA( color.getRed() * ( 0.2f + randy.nextFloat() * 0.8f ) / 255f, color.getGreen() * ( 0.2f + randy.nextFloat() * 0.8f ) / 255f, color.getBlue() * ( 0.2f + randy.nextFloat() * 0.8f ) / 255f, 1f );
			Material mat = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );
			
			mat.setColor( "Diffuse", col );
			mat.setColor( "Ambient", col );
			mat.setBoolean( "UseMaterialColors", true );

			geom.setMaterial( mat );
			geom.setLocalTranslation( (float) p1.x, (float) p1.y, (float) p1.z );
			gNode.attachChild( geom );
			
			for (Street street : p1.streets ) {

				Junction p2 = street.getOther(p1);

				Line line = new Line ( Jme3z.to( p1 ), Jme3z.to( p2) );
				Geometry lg = new Geometry("line", line);
				lg.setMaterial( lineMat );
				gNode.attachChild( lg );

				// rectangle corners
				Vector3f vector = new Vector3f((float)-(p2.z-p1.z), (float)(p2.y-p1.y), (float)(p2.x-p1.x));
				Vector3f v = new Vector3f(vector.x*(4/vector.length()), vector.y*(4/vector.length()), vector.z*(4/vector.length()));

				street.c1 = new Point3d((p1.x+v.x), (p1.y+v.y), (p1.z+v.z));
				street.c2 = new Point3d((p1.x-v.x), (p1.y-v.y), (p1.z-v.z));
				street.c3 = new Point3d((p2.x+v.x), (p2.y+v.y), (p2.z+v.z));
				street.c4 = new Point3d((p2.x-v.x), (p2.y-v.y), (p2.z-v.z));

				// line mesh
				Mesh m = new Mesh();
				m.setMode(Mesh.Mode.Lines);

				List<Float> coords = new ArrayList();
				//List<Integer> inds = new ArrayList();

				//inds.add( inds.size() );

//				coords.add( (float) street.c1.x );
//				coords.add( (float) c1.y );
//				coords.add( (float) c1.z );
//				coords.add( (float) c2.x );
//				coords.add( (float) c2.y );
//				coords.add( (float) c2.z );
//				coords.add( (float) c3.x );
//				coords.add( (float) c3.y );
//				coords.add( (float) c3.z );
//				coords.add( (float) c4.x );
//				coords.add( (float) c4.y );
//				coords.add( (float) c4.z );

				m.setBuffer( VertexBuffer.Type.Position, 3, Arrayz.toFloatArray( coords ) );
				//m.setBuffer( VertexBuffer.Type.Index, 2, Arrayz.toIntArray(inds) );

				Geometry mg = new Geometry("mesh", m);
				mg.setCullHint(  Spatial.CullHint.Never );
				Material lineMaterial = new Material( tweed.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md" );
				lineMaterial.setColor( "Color", new ColorRGBA( 0, 1f, 1f, 1f ) );
				mg.setMaterial( lineMaterial );

				mg.setLocalTranslation( 0, 0, 0 );
				gNode.attachChild( mg );

				// intersections
				List<Street> temp = street.createTempStreets(street.getJ1(), street.getJ2());

				Box i = new Box(1f, 1f, 1f);
				ColorRGBA coli = new ColorRGBA( 1f, 1f , 1f, 1f );
				Material mati = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );

				mati.setColor( "Diffuse", coli );
				mati.setColor( "Ambient", coli );
				mati.setBoolean( "UseMaterialColors", true );

                if (temp.get(1) != null) {
                    Point3d intrsct2 = street.intersect(temp.get(0).getC1(), temp.get(0).getC3(), temp.get(1).getC2(), temp.get(1).getC4());
                    street.c1 = intrsct2;
                }

				if (temp.get(2) != null) {
					Point3d intrsct1 = street.intersect(temp.get(0).getC2(), temp.get(0).getC4(), temp.get(2).getC1(), temp.get(2).getC3());
					street.c2 = intrsct1;
				}

                if (temp.get(3) != null) {
                    Point3d intrsct4 = street.intersect(temp.get(0).getC4(), temp.get(0).getC2(), temp.get(3).getC3(), temp.get(3).getC1());
                    street.c4 = intrsct4;
                }

				if (temp.get(4) != null) {
					Point3d intrsct3 = street.intersect(temp.get(0).getC3(), temp.get(0).getC1(), temp.get(4).getC4(), temp.get(4).getC2());
					street.c3 = intrsct3;
				}

//				for (Point3d p : new Point3d[] { street.c1} ) {
				for (Point3d p : new Point3d[] { street.c1, street.c2, street.c3, street.c4} ) {
					Geometry gi = new Geometry("box", i);
					gi.setMaterial(mati);
					gi.setLocalTranslation( (float) p.x, (float) p.y, (float) p.z );
					gNode.attachChild( gi );
				}

				// street mesh
				Mesh mesh = new Mesh();

				Vector3f[] vertices = new Vector3f[4];
				vertices[0] = new Vector3f((float) street.c2.x, (float) street.c2.y, (float) street.c2.z);
				vertices[1] = new Vector3f((float) street.c4.x, (float) street.c4.y, (float) street.c4.z);
				vertices[2] = new Vector3f((float) street.c1.x, (float) street.c1.y, (float) street.c1.z);
				vertices[3] = new Vector3f((float) street.c3.x, (float) street.c3.y, (float) street.c3.z);

				Vector2f[] texCoord = new Vector2f[4];
				texCoord[0] = new Vector2f(0,0);
				texCoord[1] = new Vector2f(1,0);
				texCoord[2] = new Vector2f(0,1);
				texCoord[3] = new Vector2f(1,1);

				int[] indices = { 2,3,1, 1,0,2 };

				mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
				mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
				mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indices));
				//mesh.updateBound();

				Geometry geo = new Geometry("mesh", mesh);
				geo.setCullHint(  Spatial.CullHint.Never );
				Material recmat = new Material(tweed.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                Texture tex = tweed.getAssetManager().loadTexture("road.jpg");
                recmat.setTexture("ColorMap", tex);
//				recmat.setColor("Color", new ColorRGBA( 0, 0, 1f, 1f ));
				geo.setMaterial(recmat);
				gNode.attachChild(geo);

			}

			System.out.println("New junction:");
			for (Street s : p1.streets) {
				System.out.println(s.getVector());
			}
			System.out.println("");

		}
	}


	@Override
	public JComponent getUI() {
		return new JLabel("Graph");
	}

}
