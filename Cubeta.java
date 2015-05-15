import java.io.IOException;
import java.io.RandomAccessFile;

public class Cubeta {
	
	public static final int CUBETAM = 2500;
	
	public RegCubeta registro = null;
	private RandomAccessFile raf = null;
	public RegCubeta[] registros = new RegCubeta[CUBETAM];
	
	public int genCubeta;
	public int lastIndex;
	public byte[] clavedeTabla = new byte[20];
	
	public Cubeta(RandomAccessFile indice) {
		raf = indice;
		registro = new RegCubeta();
	}
	
	public Cubeta() {
		clavedeTabla = new byte[ clavedeTabla.length ];
		RegCubeta registro = new RegCubeta();
	}

	public int getLastIndex() {
		return this.lastIndex;
	}

	public void setLastIndex(int lastIndex) {
		this.lastIndex = lastIndex;
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
		raf.seek(pos+clavedeTabla.length+8);
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
		return ((Integer.SIZE / 8)*2)+(20)+(25*CUBETAM);
	}
	
	public void read( RandomAccessFile raf ) throws IOException {
		genCubeta = raf.readInt();
		lastIndex = raf.readInt();
		raf.read(clavedeTabla);
		for(int i = 0; i<CUBETAM;i++) {
			RegCubeta nuevo = new RegCubeta();
			nuevo.read(raf);
			registros[i] = nuevo;
		}
	}
	
	public void write( RandomAccessFile raf) throws IOException {
		raf.writeInt(genCubeta);
		raf.writeInt(lastIndex);
		raf.write(clavedeTabla);
		for(int i = 0; i<CUBETAM;i++) {
			registro = registros[i];
			registro.write(raf);
		}
	}
	
	public void crearCubeta(int pos, int generacion, String clave) throws IOException {
		this.genCubeta = generacion;
		this.lastIndex = 0;
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
		String asocTablaTemp = Integer.toBinaryString(repreBin);
		String claveActual = this.getClavedeTabla();
		claveActual = claveActual.trim();
		String clavecubeta1 = "0" + claveActual;
		String clavecubeta2 = "1" + claveActual;
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
		raf.seek(pos);
		this.read(raf);
		RegCubeta[] temporales = this.registros;
		String claveBuena = this.getClavedeTabla().trim();
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

			}
		}
		this.registros = temporales;
		raf.seek(pos);
		this.lastIndex = 0;
		this.write(raf);
	}
	
	
	public void busquedaLineal(String clave) throws IOException
        {
           boolean encontrado=false;
            for(int i=0; i<CUBETAM; i++)
            {
                if(this.registros[i].getCodigo().equals(clave))
                {
                    System.out.println("El registro se encuentra en posición: " + this.registros[i].getLiga());
                   
                    i=CUBETAM;
                    encontrado = true;
                }
                
            }
            
            if(!encontrado)
                System.out.println("El registro no existe");
        }
        
        public void eliminar(String clave, int posicion) throws IOException
        {
            boolean encontrado=false;
            for(int i=0; i<CUBETAM; i++)
            {
                if(this.registros[i].getCodigo().equals(clave))
                {
                    
                    this.setLastIndex(0);
                    this.registros[i].setEstado((byte)0);
                    this.registros[i].setCodigo("");
                    this.registros[i].setLiga(-1);
                    i=CUBETAM;
                    encontrado = true;
                }
                
            }
            
            if(!encontrado)
                ;
            else
            {
                this.escribirCubeta(posicion);
            }

        }
        
        public int buscaElimina(String clave)
        {
            boolean encontrado=false;
            for(int i=0; i<CUBETAM; i++)
            {
                if(this.registros[i].getCodigo().equals(clave))
                {
                    System.out.println("El registro se encuentra en posición: " + this.registros[i].getLiga()
                    + " se eliminará");
                    
                    return this.registros[i].getLiga();
                }
            }
            
            if(!encontrado)
                System.out.println("El registro no existe");
            return -1;

        }
}
