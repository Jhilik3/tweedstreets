package org.twak.tweed.gen;

import java.io.File;
import java.util.*;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

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
//		myGraph.put( new Point3d(0,0,0), new Point3d(-110, 0, 0));
//		myGraph.put( new Point3d(-150,0,0), new Point3d(-110, 0, 0));
//		myGraph.put( new Point3d(0,0,0), new Point3d(0, 0, -80));
//		myGraph.put( new Point3d(0,0,0), new Point3d(10, 0, 50));
//
//
////		myGraph.put( new Point3d(100, 0, 0), new Point3d(0,0,0));
////		myGraph.put( new Point3d(0, 0, 100), new Point3d(0,0,0));
////		myGraph.put( new Point3d(-150, 0, 0), new Point3d(0,0,0));
////		myGraph.put( new Point3d(0, 0, -170), new Point3d(0,0,0));
//
//		myGraph.put( new Point3d(100, 0, 0), new Point3d(100,0,50));
//		myGraph.put( new Point3d(100, 0, 0), new Point3d(100,0,-40));
//		myGraph.put( new Point3d(100, 0, 0), new Point3d(130,0,0));
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

		Set<Street> streetSet = new HashSet<>();

		for ( Junction p1 : jns ) {

			Box jbox = new Box( 1f, 1f, 1f );
			Geometry juncGeom = new Geometry( "Box", jbox );

			juncGeom.setUserData( EventMoveHandle.class.getSimpleName(), new Object[] { new EventMoveHandle() {
				@Override
				public void posChanged() {

//					List<Point3d> to = graph.get( p1 );
//					graph.remove( p1 );
					p1.set( Jme3z.from( juncGeom.getLocalTranslation() ) );
//					graph.putAll( p1, to );
					calculate();
				}
			} } );

			ColorRGBA col = new ColorRGBA( color.getRed() * ( 0.2f + randy.nextFloat() * 0.8f ) / 255f, color.getGreen() * ( 0.2f + randy.nextFloat() * 0.8f ) / 255f, color.getBlue() * ( 0.2f + randy.nextFloat() * 0.8f ) / 255f, 1f );
			Material mat = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );
			
			mat.setColor( "Diffuse", col );
			mat.setColor( "Ambient", col );
			mat.setBoolean( "UseMaterialColors", true );

			juncGeom.setMaterial( mat );
			juncGeom.setLocalTranslation( (float) p1.x, (float) p1.y, (float) p1.z );
			gNode.attachChild( juncGeom );


			for (Street street : new ArrayList<>(p1.streets)) {

				if (streetSet.contains(street)) {
					continue;
				}
				streetSet.add(street);

				Junction p2 = street.getOther(p1);

				// centreline
				Line line = new Line ( Jme3z.to( p1 ), Jme3z.to( p2) );
				Geometry lg = new Geometry("line", line);
				lg.setMaterial( lineMat );
//				gNode.attachChild( lg );

				// street corners
				Vector3f vector = new Vector3f((float)-(p2.z-p1.z), (float)(p2.y-p1.y), (float)(p2.x-p1.x));
				Vector3f v = new Vector3f(vector.x*(4/vector.length()), vector.y*(4/vector.length()), vector.z*(4/vector.length()));

				street.c1 = new Point3d((p1.x+v.x), (p1.y+v.y), (p1.z+v.z));
				street.c2 = new Point3d((p1.x-v.x), (p1.y-v.y), (p1.z-v.z));
				street.c3 = new Point3d((p2.x+v.x), (p2.y+v.y), (p2.z+v.z));
				street.c4 = new Point3d((p2.x-v.x), (p2.y-v.y), (p2.z-v.z));

//				debugLineMesh(street);
				createStreetMesh(street);


			}

			// junction polygons
			if (p1.streets.size() > 2) {
				List<Point3d> intersects = new ArrayList();
				List<Street> jstreet = new ArrayList<>(p1.getOutwardsGoingStreets());

				for (Street s : jstreet) {
					s.corners();
				}

				for (Street s : jstreet) {
					int index = jstreet.indexOf(s);
					index = (index + 1) % jstreet.size();
					Street t = jstreet.get(index);
					Point3d i = s.intersect(s.getC1(), s.getC3(), t.getC2(), t.getC4());
					intersects.add(i);
				}

				// junction mesh
				Mesh m = new Mesh();
				Vector3f[] vert = new Vector3f[intersects.size()];
				Vector2f[] textCoord = new Vector2f[intersects.size()];
				int i = 0;
				for (Point3d in : intersects) {
					vert[i] = new Vector3f((float)in.x, -1, (float)in.z);
					textCoord[i] = new Vector2f(0,0);
					i++;
				}

				int[] indices = new int[(intersects.size() - 2)*3];

				for (int j = 0, c = 1; j < indices.length; j += 3, c++) {
					indices[j] = 0;
					indices[j+1] = c+1;
					indices[j+2] = c;
				}

				if (indices != null) {
					m.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vert));
					m.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(textCoord));
					m.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indices));
					//mesh.updateBound();

					Geometry g = new Geometry("mesh", m);
					g.setCullHint(  Spatial.CullHint.Never );
					Material jmat = new Material(tweed.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
//					Texture text = tweed.getAssetManager().loadTexture("road.jpg");
//					jmat.setTexture("ColorMap", text);
					jmat.setColor("Color", new ColorRGBA( 0, 0, 1f, 1f ));
					g.setMaterial(jmat);
					gNode.attachChild(g);
				}
			}
		}
	}

	private void createStreetMesh(Street street) {
		List<Street> temp = street.createTempStreets(
				street.getJ1().getOutwardsGoingStreets(),
				street.getJ2().getInwardsGoingStreets ());

//		debugTempstreets(temp,street);

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
//					gNode.attachChild( gi );
				}

		// street mesh
		Mesh mesh = new Mesh();

		Vector3f[] vertices = new Vector3f[4];
		vertices[0] = new Vector3f((float) street.c2.x, (float) -1, (float) street.c2.z);
		vertices[1] = new Vector3f((float) street.c4.x, (float) -1, (float) street.c4.z);
		vertices[2] = new Vector3f((float) street.c1.x, (float) -1, (float) street.c1.z);
		vertices[3] = new Vector3f((float) street.c3.x, (float) -1, (float) street.c3.z);

		Vector2f[] texCoord = new Vector2f[4];
		texCoord[0] = new Vector2f(0,0);
		texCoord[1] = new Vector2f((float) street.getLength()/15,0);
		texCoord[2] = new Vector2f(0,1);
		texCoord[3] = new Vector2f((float) street.getLength()/15,1);

		int[] indices = { 2,3,1, 1,0,2 };

		mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
		mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
		mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indices));
		mesh.updateBound();

		Geometry geo = new Geometry("mesh", mesh);
		geo.setCullHint(  Spatial.CullHint.Never );
		Material recmat = new Material(tweed.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		Texture tex = tweed.getAssetManager().loadTexture("road2.jpg");
		tex.setWrap(Texture.WrapMode.Repeat);
		recmat.setTexture("ColorMap", tex);
		geo.setMaterial(recmat);
		gNode.attachChild(geo);
	}

	private void debugLineMesh(Street street) {
		// line mesh
		Mesh m = new Mesh();
		m.setMode(Mesh.Mode.Lines);

		List<Float> coords = new ArrayList();
//				List<Integer> inds = new ArrayList();

//				inds.add(  );		//inds.size()

		coords.add( (float) street.c1.x );
		coords.add( (float) street.c1.y );
		coords.add( (float) street.c1.z );
		coords.add( (float) street.c2.x );
		coords.add( (float) street.c2.y );
		coords.add( (float) street.c2.z );
		coords.add( (float) street.c3.x );
		coords.add( (float) street.c3.y );
		coords.add( (float) street.c3.z );
		coords.add( (float) street.c4.x );
		coords.add( (float) street.c4.y );
		coords.add( (float) street.c4.z );

		m.setBuffer( VertexBuffer.Type.Position, 3, Arrayz.toFloatArray( coords ) );
		m.setBuffer( VertexBuffer.Type.Index, 4, new short[] {0,1,2,3});
//				m.setBuffer( VertexBuffer.Type.Index, 2, Arrayz.toIntArray(inds) );

		Geometry mg = new Geometry("mesh", m);
		mg.setCullHint(  Spatial.CullHint.Never );
		Material lineMaterial = new Material( tweed.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md" );
		lineMaterial.setColor( "Color", new ColorRGBA( 0, 1f, 1f, 1f ) );
		mg.setMaterial( lineMaterial );

		mg.setLocalTranslation( 0, 0, 0 );
		gNode.attachChild( mg );
	}

	private void debugTempstreets(List<Street> temp, Street street) {

		ColorRGBA colj = new ColorRGBA( (float)Math.random(),(float)Math.random(),(float)Math.random(), 1f );
		Material matj = new Material( tweed.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md" );
		matj.setColor( "Diffuse", colj );
		matj.setColor( "Ambient", colj );
		matj.setBoolean( "UseMaterialColors", true );

		for (Street t : temp) {

			if (t != null) {
				javax.vecmath.Vector3f dir = t.getVector();

				float height = 0;
				if (t == street) {
					height = 15;
					dir.scale(-0.5f );
					dir.add (new javax.vecmath.Vector3f((float) street.getP1().x, (float) street.getP1().y, (float) street.getP1().z) );
				}
				else if (street.getP1().equals ( t.getP1())) {
					height = 5;
					dir.scale(-(float)(Math.random() * 10 + 5) / dir.length());
					dir.add(new javax.vecmath.Vector3f((float) street.getP1().x, (float) street.getP1().y, (float) street.getP1().z));
				}
				else if (street.getP2().equals ( t.getP2())) {
					height = 10;
					dir.scale((float)(Math.random() * 10 + 5) / dir.length());
					dir.add(new javax.vecmath.Vector3f((float) street.getP2().x, (float) street.getP2().y, (float) street.getP2().z));
				} else {
					System.out.println();
				}

				Box j = new Box(1f, height, 1f);
				Geometry gi = new Geometry("box", j);
				gi.setMaterial(matj);
				gi.setLocalTranslation(dir.x, dir.y, dir.z);
				gNode.attachChild(gi);
			}
		}
	}

	@Override
	public JComponent getUI() {
		return new JLabel("Graph");
	}

}
