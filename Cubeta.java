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
	
	public Cubeta() {
		clavedeTabla = new byte[ clavedeTabla.length ];
		RegCubeta registro = new RegCubeta();
	}
	
	public Cubeta leerCubeta(int pos) throws IOException{
		this.raf.seek(pos);
		this.read(this.raf);
		return this;
	}
	
	public RegCubeta[] getRegistros() {
		return this.registros;
	}
	
	public int getrafLength() throws IOException{
		return (int)raf.length();
	}
	
	public RegCubeta[] getRegistrosForzado(int pos) throws IOException {
		raf.seek(pos+clavedeTabla.length+4);
		RegCubeta[] forzado = new RegCubeta[CUBETAM];
		for(int i = 0; i<CUBETAM; i++) {
			RegCubeta tempo = new RegCubeta();
			tempo.read(raf);
			forzado[i] = tempo;
		}
		return forzado;
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
		return (Integer.SIZE / 8)+(20)+(25*CUBETAM);
	}
	
	public void read( RandomAccessFile raf ) throws IOException {
		genCubeta = raf.readInt();
		raf.read(clavedeTabla);
		for(int i = 0; i<CUBETAM;i++) {
			RegCubeta nuevo = new RegCubeta();
			nuevo.read(raf);
			registros[i] = nuevo;
		}
	}
	
	public void write( RandomAccessFile raf) throws IOException {
		raf.writeInt(genCubeta);
		raf.write(clavedeTabla);
		for(int i = 0; i<CUBETAM;i++) {
			registro = registros[i];
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
	}
	
	public void escribirCubeta(int pos) throws IOException {
		raf.seek(pos);
		write(raf);
	}
	
	private void insertarCubeta(int posicion) throws IOException {
		int n = (int) raf.length() / this.cubetaSize();
		for( int i = n-1; i >= posicion; i -- ) {
			Cubeta temp = new Cubeta();
			raf.seek( i * this.cubetaSize() );
			temp.read( raf );
			raf.seek( (i+1) * this.cubetaSize() );
			temp.write( raf );
		}
		raf.seek( posicion * this.cubetaSize() );
		this.write( raf );
	}
	
	public void split(int pos) throws IOException{
		System.out.println("[CUBETA - split] LLAMADO A SPLIT------------------------------------");
		int n = (int) raf.length() / this.cubetaSize();
		this.genCubeta++;
		//Numero binario que se usara como clave de asociación a la tabla de hash
		int repreBin = (int)Math.pow(2, this.genCubeta); 
		System.out.println("[CUBETA - split] valor de repreBin: " + repreBin);
		String asocTablaTemp = Integer.toBinaryString(repreBin);
		System.out.println("[CUBETA - split] valor de asocTablaTempo: " + asocTablaTemp);
		String claveActual = this.getClavedeTabla();
		System.out.println("[CUBETA - split] valor de claveActual: " + claveActual);
		claveActual = claveActual.trim();
		String clavecubeta1 = "0" + claveActual;
		String clavecubeta2 = "1" + claveActual;
		System.out.println("[CUBETA - split] Al final la clave1 es: " +  clavecubeta1 + " clave2:" + clavecubeta2);
		raf.seek(pos);
		this.setClavedeTabla(clavecubeta1);
		write(raf);
		this.setClavedeTabla(clavecubeta2);
		//this.insertarCubeta((int)raf.length()/(pos+this.cubetaSize()));
		this.insertarCubeta((pos+this.cubetaSize())/this.cubetaSize());
		n = (int) raf.length() / this.cubetaSize();
		this.organizarRegistros(pos);
		this.organizarRegistros(pos+this.cubetaSize());
	}
	
	public void organizarRegistros(int pos) throws IOException{
		System.out.println("[CUBETA - org] Posicion inicial: " + pos);
		raf.seek(pos);
		this.read(raf);
		RegCubeta[] temporales = this.registros;
		String claveBuena = this.getClavedeTabla().trim();
		System.out.println("[CUBETA - org] La clave buena es: " + claveBuena);
		for(int i = 0; i <temporales.length; i++) {
			RegCubeta temp = new RegCubeta();
			temp = temporales[i];
			String claveTemporal = temp.getCodigo().trim();
			claveTemporal = claveTemporal.substring(claveTemporal.length()-this.genCubeta);
			if(!claveBuena.equals(claveTemporal)) {
				temp.setEstado((byte)0);
				temp.setCodigo("");
				temp.setLiga(-1);
				temp = temporales[i];
			} else {
				System.out.println("[CUBETA - org]La clave buena sí es buena: " +  claveBuena + " claveTemporal: " + claveTemporal);
			}
		}
		this.registros = temporales;
		raf.seek(pos);
		this.write(raf);
	}
}
