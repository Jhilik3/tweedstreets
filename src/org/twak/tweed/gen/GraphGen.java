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
	
//	public Graph3D graph;

	public Set<Junction> jns;

	public GraphGen() {}
	public GraphGen( File source, Tweed tweed ) {
		super( Filez.stripExtn( source.getName( )), tweed);
		this.source = source;

		CoordinateReferenceSystem crss = Tweed.kludgeCMS.get( TweedSettings.settings.gmlCoordSystem );
		Graph3D graph = GMLReader.readGMLGraph( Tweed.toWorkspace( source ), DefaultGeocentricCRS.CARTESIAN, crss );
		
		graph.transform ( TweedSettings.settings.toOrigin );

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

				// midpoint
//				Point3d p3 = new Point3d((p2.x+p1.x)/2, (p2.y+p1.y)/2, (p2.z+p1.z)/2);
//				Box box2 = new Box(2f, 2f, 2f);
//				Geometry bg = new Geometry("box", box2);

//				ColorRGBA col2 = new ColorRGBA( 1f, 1f , 0, 1f );
//				Material mat2 = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );
//
//				mat2.setColor( "Diffuse", col2 );
//				mat2.setColor( "Ambient", col2 );
//				mat2.setBoolean( "UseMaterialColors", true );

//				bg.setMaterial(mat2);
//				bg.setLocalTranslation( (float) p3.x, (float) p3.y, (float) p3.z );
//				gNode.attachChild( bg );

				// rectangle corners
				Vector3f vector = new Vector3f((float)-(p2.z-p1.z), (float)(p2.y-p1.y), (float)(p2.x-p1.x));
				Vector3f v = new Vector3f(vector.x*(4/vector.length()), vector.y*(4/vector.length()), vector.z*(4/vector.length()));

				Point3d c1 = new Point3d((p1.x+v.x), (p1.y+v.y), (p1.z+v.z));
				Point3d c2 = new Point3d((p1.x-v.x), (p1.y-v.y), (p1.z-v.z));
				Point3d c3 = new Point3d((p2.x+v.x), (p2.y+v.y), (p2.z+v.z));
				Point3d c4 = new Point3d((p2.x-v.x), (p2.y-v.y), (p2.z-v.z));

				Box b1 = new Box(1f, 1f, 1f);
				Box b2 = new Box(1f, 1f, 1f);
				Box b3 = new Box(1f, 1f, 1f);
				Box b4 = new Box(1f, 1f, 1f);
				Geometry g1 = new Geometry("box", b1);
				Geometry g2 = new Geometry("box", b2);
				Geometry g3 = new Geometry("box", b3);
				Geometry g4 = new Geometry("box", b4);

				ColorRGBA col3 = new ColorRGBA( 0, 1f , 1f, 1f );
				Material mat3 = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );

				mat3.setColor( "Diffuse", col3 );
				mat3.setColor( "Ambient", col3 );
				mat3.setBoolean( "UseMaterialColors", true );

//				g1.setMaterial(mat3);
//				g2.setMaterial(mat2);
//				g3.setMaterial(mat3);
//				g4.setMaterial(mat);
//				g1.setLocalTranslation( (float) c1.x, (float) c1.y, (float) c1.z );
//				g2.setLocalTranslation( (float) c2.x, (float) c2.y, (float) c2.z );
//				g3.setLocalTranslation( (float) c3.x, (float) c3.y, (float) c3.z );
//				g4.setLocalTranslation( (float) c4.x, (float) c4.y, (float) c4.z );
//				gNode.attachChild( g1 );
//				gNode.attachChild( g2 );
//				gNode.attachChild( g3 );
//				gNode.attachChild( g4 );

				// line mesh
				Mesh m = new Mesh();
				m.setMode(Mesh.Mode.Lines);

				List<Float> coords = new ArrayList();
				//List<Integer> inds = new ArrayList();

				//inds.add( inds.size() );

				coords.add( (float) c1.x );
				coords.add( (float) c1.y );
				coords.add( (float) c1.z );
				coords.add( (float) c2.x );
				coords.add( (float) c2.y );
				coords.add( (float) c2.z );
				coords.add( (float) c3.x );
				coords.add( (float) c3.y );
				coords.add( (float) c3.z );
				coords.add( (float) c4.x );
				coords.add( (float) c4.y );
				coords.add( (float) c4.z );

				m.setBuffer( VertexBuffer.Type.Position, 3, Arrayz.toFloatArray( coords ) );
				//m.setBuffer( VertexBuffer.Type.Index, 2, Arrayz.toIntArray(inds) );

				Geometry mg = new Geometry("mesh", m);
				mg.setCullHint(  Spatial.CullHint.Never );
				Material lineMaterial = new Material( tweed.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md" );
				lineMaterial.setColor( "Color", new ColorRGBA( 0, 1f, 1f, 1f ) );
				mg.setMaterial( lineMaterial );

				mg.setLocalTranslation( 0, 0, 0 );
				gNode.attachChild( mg );

				// rectangle mesh
//				Mesh mesh = new Mesh();
//
//				Vector3f[] vertices = new Vector3f[4];
//				vertices[0] = new Vector3f((float) c2.x, (float) c2.y, (float) c2.z);
//				vertices[1] = new Vector3f((float) c4.x, (float) c4.y, (float) c4.z);
//				vertices[2] = new Vector3f((float) c1.x, (float) c1.y, (float) c1.z);
//				vertices[3] = new Vector3f((float) c3.x, (float) c3.y, (float) c3.z);
//
//				Vector2f[] texCoord = new Vector2f[4];
//				texCoord[0] = new Vector2f(0,0);
//				texCoord[1] = new Vector2f(1,0);
//				texCoord[2] = new Vector2f(0,1);
//				texCoord[3] = new Vector2f(1,1);
//
//				int[] indices = { 2,3,1, 1,0,2 };
//
//				mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
//				mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
//				mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indices));
//				//mesh.updateBound();
//
//				Geometry geo = new Geometry("mesh", mesh);
//				geo.setCullHint(  Spatial.CullHint.Never );
//				Material recmat = new Material(tweed.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
//				recmat.setColor("Color", new ColorRGBA( 0, 0, 1f, 1f ));
//				geo.setMaterial(recmat);
//				gNode.attachChild(geo);

				// intersections
				List<Street> temp = street.createTempStreets();

				Box i = new Box(1f, 1f, 1f);
				ColorRGBA coli = new ColorRGBA( 1f, 1f , 1f, 1f );
				Material mati = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );

				mati.setColor( "Diffuse", coli );
				mati.setColor( "Ambient", coli );
				mati.setBoolean( "UseMaterialColors", true );

				if (temp.get(1) != null) {
					Point3d intrsct1 = street.intersect(temp.get(0).getC2(), temp.get(0).getC4(), temp.get(1).getC1(), temp.get(1).getC3());
					street.c2 = intrsct1;
					Geometry gi1 = new Geometry("box", i);
					gi1.setMaterial(mati);
					gi1.setLocalTranslation( (float) intrsct1.x, (float) intrsct1.y, (float) intrsct1.z );
					gNode.attachChild( gi1 );
				}

				if (temp.get(2) != null) {
					Point3d intrsct2 = street.intersect(temp.get(0).getC1(), temp.get(0).getC3(), temp.get(2).getC2(), temp.get(2).getC4());
					street.c1 = intrsct2;
					Geometry gi2 = new Geometry("box", i);
					gi2.setMaterial(mati);
					gi2.setLocalTranslation( (float) intrsct2.x, (float) intrsct2.y, (float) intrsct2.z );
					gNode.attachChild( gi2 );
				}

				if (temp.get(3) != null) {
					Point3d intrsct3 = street.intersect(temp.get(0).getC1(), temp.get(0).getC3(), temp.get(3).getC2(), temp.get(3).getC4());
					street.c3 = intrsct3;
					Geometry gi3 = new Geometry("box", i);
					gi3.setMaterial(mati);
					gi3.setLocalTranslation( (float) intrsct3.x, (float) intrsct3.y, (float) intrsct3.z );
					gNode.attachChild( gi3 );
				}

				if (temp.get(4) != null) {
					Point3d intrsct4 = street.intersect(temp.get(0).getC2(), temp.get(0).getC4(), temp.get(4).getC1(), temp.get(4).getC3());
					street.c4 = intrsct4;
					Geometry gi4 = new Geometry("box", i);
					gi4.setMaterial(mati);
					gi4.setLocalTranslation( (float) intrsct4.x, (float) intrsct4.y, (float) intrsct4.z );
					gNode.attachChild( gi4 );
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
				recmat.setColor("Color", new ColorRGBA( 0, 0, 1f, 1f ));
				geo.setMaterial(recmat);
				gNode.attachChild(geo);

			}

			System.out.println("New junction:");
			for (Street s : p1.streets) {
				System.out.println(s.getVector());
			}
			System.out.println("");

//			if (p1.streets.size() == 1) {
//				Box j1 = new Box(3f, 3f, 3f);
//				Geometry g = new Geometry("box", j1);
//				ColorRGBA colj = new ColorRGBA( 1f, 1f , 1f, 1f );
//				Material matj = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );
//
//				matj.setColor( "Diffuse", colj );
//				matj.setColor( "Ambient", colj );
//				matj.setBoolean( "UseMaterialColors", true );
//
//				g.setMaterial(matj);
//				g.setLocalTranslation( (float) p1.x, (float) p1.y, (float) p1.z );
//				gNode.attachChild( g );
//			}

			// intersections
//			if (p1.streets.size() == 2) {
//				Street s1 = p1.streets.get(0);
//				Junction o1 = s1.getOther(p1);
//
//				Vector3f perp1 = new Vector3f((float)-(o1.z-p1.z), (float)(o1.y-p1.y), (float)(o1.x-p1.x));
//				Vector3f vec1 = new Vector3f(perp1.x*(4/perp1.length()), perp1.y*(4/perp1.length()), perp1.z*(4/perp1.length()));
//
//				Point3d cor1 = new Point3d((p1.x+vec1.x), (p1.y+vec1.y), (p1.z+vec1.z));
//				Point3d cor2 = new Point3d((p1.x-vec1.x), (p1.y-vec1.y), (p1.z-vec1.z));
//				Point3d cor3 = new Point3d((o1.x+vec1.x), (o1.y+vec1.y), (o1.z+vec1.z));
//				Point3d cor4 = new Point3d((o1.x-vec1.x), (o1.y-vec1.y), (o1.z-vec1.z));
//
//				Street s2 = p1.streets.get(1);
//				Junction o2 = s2.getOther(p1);
//
//				Vector3f perp2 = new Vector3f((float)-(o2.z-p1.z), (float)(o2.y-p1.y), (float)(o2.x-p1.x));
//				Vector3f vec2 = new Vector3f(perp2.x*(4/perp2.length()), perp2.y*(4/perp2.length()), perp2.z*(4/perp2.length()));
//
//				Point3d co1 = new Point3d((p1.x+vec2.x), (p1.y+vec2.y), (p1.z+vec2.z));
//				Point3d co2 = new Point3d((p1.x-vec2.x), (p1.y-vec2.y), (p1.z-vec2.z));
//				Point3d co3 = new Point3d((o2.x+vec2.x), (o2.y+vec2.y), (o2.z+vec2.z));
//				Point3d co4 = new Point3d((o2.x-vec2.x), (o2.y-vec2.y), (o2.z-vec2.z));
//
//				double denom = (cor1.x - cor3.x)*(co1.z - co3.z) - (cor1.z - cor3.z)*(co1.x - co3.x);
//				Point3d intersect = new Point3d(((cor1.x*cor3.z - cor1.z*cor3.x)*(co1.x-co3.x) - (cor1.x-cor3.x)*(co1.x*co3.z - co1.z*co3.x))/denom, 0,
//						((cor1.x*cor3.z - cor1.z*cor3.x)*(co1.z-co3.z) - (cor1.z-cor3.z)*(co1.x*co3.z - co1.z*co3.x))/denom);
//
//				System.out.println(intersect.toString());
//
//				Box i = new Box(1f, 1f, 1f);
//				Geometry gi = new Geometry("box", i);
//				ColorRGBA coli = new ColorRGBA( 1f, 1f , 1f, 1f );
//				Material mati = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );
//
//				mati.setColor( "Diffuse", coli );
//				mati.setColor( "Ambient", coli );
//				mati.setBoolean( "UseMaterialColors", true );
//
//				gi.setMaterial(mati);
//				gi.setLocalTranslation( (float) intersect.x, (float) intersect.y, (float) intersect.z );
//				gNode.attachChild( gi );
//			}


		}
	}


	@Override
	public JComponent getUI() {
		return new JLabel("Graph");
	}

}
