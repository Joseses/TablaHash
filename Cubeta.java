import java.io.IOException;
import java.io.RandomAccessFile;

public class Cubeta {
	
	public static final int CUBETAM = 5;
	
	public RegCubeta registro = null;
	private RandomAccessFile raf = null;
	public RegCubeta[] registros = new RegCubeta[CUBETAM];
	
	public int genCubeta;
	public byte[] clavedeTabla = new byte[20];
	
	public Cubeta(RandomAccessFile indice) {
		raf = indice;
		registro = new RegCubeta();
	}
	
	public Cubeta() {}
	
	public Cubeta leerCubeta(int pos) throws IOException{
		this.raf.seek(pos);
		System.out.println("[CUBETA - leerCubeta] Posicion " + raf.getFilePointer());
		this.read(this.raf);
		return this;
	}
	
	public RegCubeta[] getRegistros() {
		return this.registros;
	}
	
	public int getrafLength() throws IOException{
		return (int)raf.length();
	}
	
	public void setClavedeTabla( String nom ) {
		clavedeTabla = new byte[ clavedeTabla.length ];
		if( nom.length() > clavedeTabla.length )
			System.out.println( "ATENCION: Nombre con más de 20 caracteres" );
	
		for( int i = 0; i < clavedeTabla.length && i < nom.getBytes().length; i++ )
			 clavedeTabla[i] = nom.getBytes()[i];
	}
	
	public String getClavedeTabla() { return new String( clavedeTabla ); }
	
	public int getGeneracion() {
		return this.genCubeta;
	}
	
	public int cubetaSize() {
		return (Integer.SIZE / 8)+(clavedeTabla.length)+(registro.length()*CUBETAM);
	}
	
	public void read( RandomAccessFile raf ) throws IOException {
		genCubeta = raf.readInt();
		raf.read(clavedeTabla);
		for(int i = 0; i<CUBETAM;i++) {
			RegCubeta nuevo = new RegCubeta();
			System.out.println("Entramos a for en CUBETA");
			System.out.println("[CUBETA - read] Posicion antes de leer " + raf.getFilePointer());
			nuevo.read(raf);
			System.out.println("[CUBETA - read] Estado de registro " + nuevo.getEstado());
			registros[i] = nuevo;
		}
	}
	
	public void write( RandomAccessFile raf) throws IOException {
		raf.writeInt(genCubeta);
		raf.write(clavedeTabla);
		for(int i = 0; i<CUBETAM;i++) {
			registro = registros[i];
			System.out.println("Clave de registro en cubeta " + registro.getEstado());
			registro.write(raf);
		}
	}
	
	public void crearCubeta(int pos, int generacion, String clave) throws IOException {
		this.genCubeta = generacion;
		this.setClavedeTabla(clave);
		RegCubeta[] temporales = new RegCubeta[CUBETAM];
		for(int i = 0; i<CUBETAM; i++) {
			RegCubeta temp = new RegCubeta();
			temp.setEstado((byte)0);
			temp.setLiga(-1);
			temporales[i]=temp;
		}
		this.registros = temporales;
		this.raf.seek(pos);
		this.write(raf);
		System.out.println("RAF length en cubeta " + raf.length());
	}
	
	public void escribirCubeta(int pos) throws IOException {
		raf.seek(pos);
		write(raf);
	}
	
	private void insertarCubeta(int posicion) throws IOException {
		int n = (int) raf.length() / this.cubetaSize();
		for( int i = n-1; i >= posicion; i -- ) {
			Cubeta temp = new Cubeta();
			raf.seek( i * temp.cubetaSize() );
			temp.read( raf );
			raf.seek( (i+1) * temp.cubetaSize() );
			temp.write( raf );
		}
		raf.seek( posicion * this.cubetaSize() );
		this.write( raf );
	}
	
	public void split(int pos) throws IOException{
		this.genCubeta++;
		//Numero binario que se usara como clave de asociación a la tabla de hash
		int repreBin = (int)Math.pow(2, this.genCubeta); 
		String asocTablaTemp = Integer.toBinaryString(repreBin);
		String claveActual = this.getClavedeTabla();
		claveActual = claveActual.trim();
		String clavecubeta1 = asocTablaTemp.substring(asocTablaTemp.length()-1, asocTablaTemp.length())
						+ claveActual;
		String clavecubeta2 = asocTablaTemp.substring(asocTablaTemp.length()-2, asocTablaTemp.length()-1)
						+ claveActual;
		raf.seek(pos);
		this.setClavedeTabla(clavecubeta1);
		write(raf);
		this.setClavedeTabla(clavecubeta2);
		this.insertarCubeta((int)raf.length()/(pos+this.cubetaSize()));
		this.organizarRegistros(pos);
		this.organizarRegistros(pos+this.cubetaSize());
		
	}
	
	public void organizarRegistros(int pos) throws IOException{
		raf.seek(pos);
		this.read(raf);
		RegCubeta[] temporales = this.registros;
		String claveBuena = this.getClavedeTabla().trim();
		for(int i = 0; i <temporales.length; i++) {
			RegCubeta temp = new RegCubeta();
			temp = temporales[i];
			String claveTemporal = temp.getCodigo().trim();
			System.out.println("[CUBETA - org] Codigo de cubeta: " + temp.getCodigo());
			claveTemporal = claveTemporal.substring(claveTemporal.length()-this.genCubeta);
			if(!claveBuena.equals(claveTemporal)) {
				temp.setEstado((byte)0);
				temp.setCodigo("");
				temp.setLiga(-1);
				temp = temporales[i];
			}
		}
		this.registros = temporales;
		raf.seek(pos);
		this.write(raf);
	}
}
