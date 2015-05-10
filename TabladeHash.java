import java.io.*;

public class TabladeHash {
	
	private static int tamIndice;
	public RegIndice registro = null;
    private RandomAccessFile raf = null;
    private Cubeta cubetas = null;
	
	public TabladeHash(RandomAccessFile indice, RandomAccessFile cubetas) {
		raf = indice;
		registro = new RegIndice();
		this.cubetas = new Cubeta(cubetas);
		tamIndice = 0;
	}
	
	public void cerrar() throws IOException {
		raf.close();
	}
	
	public String funcionHash(int clave){
		String x = "";
		x = Integer.toBinaryString(clave);
		if(x.length()<20) {
			while(x.length()!=20) {
				x = "0" + x;
			}
		}
		System.out.println(x);
		return x;
	}
	
	public void insertarEntrada(Registro archRegistro, int pos) throws IOException {
		String clave = funcionHash(archRegistro.getNumero());
		if(raf.length()==0) { //No existe ninguna entrada en la tabla de hash
			RegIndice temp = new RegIndice();
			temp.setClave("");
			temp.setLiga(0);
			temp.write(raf);
			//Creamos una cubeta con el valor asociado del registro de la tabla 
			System.out.println("[TABLA - insertarEntrada] ANTES DE CREAR CUBETA");
			cubetas.crearCubeta(0, 0, temp.getClave()); 
			System.out.println("[TABLA - insertarEntrada] DESPUES DE CREAR CUBETA");
			System.out.println("[TABLA - insertarEntrada] Tamaño de cubeta" + cubetas.cubetaSize());
			System.out.println("[TABLA - insertarEntrada] ANTES DE LEER CUBETA");
			cubetas = cubetas.leerCubeta(0);
			System.out.println("[TABLA - insertarEntrada] DESPUES DE LEER CUBETA");
			RegCubeta[] temporal = cubetas.getRegistros();
			RegCubeta nueva = new RegCubeta();
			System.out.println("[TABLA - insertarEntrada] ESTADO DESPUES DE DECLARAR 'NUEVA' " + nueva.getEstado());
			nueva.setEstado((byte)1);
			nueva.setCodigo(clave);
			nueva.setLiga(pos);
			temporal[0] = nueva;
			for(int i = 0; i<temporal.length;i++) {
				RegCubeta tem = temporal[i];
				System.out.println("[TABLA - insertarEntrada] Estado de i:" + i + " " + tem.getEstado());
			}
			cubetas.registros = temporal;
			cubetas.escribirCubeta(0);
		} else if (raf.length()==registro.length()) { //Sólo existe una entrada en el registro
													//Cualquier clave puede entrar a la cubeta asociada
			cubetas = cubetas.leerCubeta(0);
			boolean hayEspacio = false;
			for(int i = 0; i<cubetas.registros.length; i++) {
				RegCubeta bus = cubetas.registros[i];
				System.out.println("[TABLA - insertarEntrada] Estado en i: " + i + " " + bus.getEstado());
				if(bus.getEstado()==0){
					System.out.println("[TABLA - insertarEntrada] Se encontró un espacio vacío en la posición: " + i);
					hayEspacio = true;
					bus.setEstado((byte)1);
					bus.setCodigo(clave);
					bus.setLiga(pos);
					cubetas.registros[i] = bus;
					i = cubetas.registros.length;
					cubetas.escribirCubeta(0);
				} //end if
			} //end for
			if(!hayEspacio) {
				System.out.println("[TABLA - insertarEntrada] Tamaño antes de split " + cubetas.getrafLength());
				cubetas.split(0);
				System.out.println("[TABLA - insertarEntrada] Tamaño después de split " + cubetas.getrafLength());
				cubetas.leerCubeta(0);
				if(cubetas.getGeneracion()>this.tamIndice) {
					duplicarTabla();
					acomodarPunteros();
				}
				insertarEntradaPost(archRegistro, pos);
			}
		} else { //Ninguno de los casos anteriores
			insertarEntradaPost(archRegistro, pos);
		}
	} //end insertarEntrada
	
	public void insertarEntradaPost(Registro archRegistro, int pos) throws IOException {
		String clave = funcionHash(archRegistro.getNumero());
		String claveRes = clave.substring(clave.length()-tamIndice);
		
		int numeroRegistros = (int)raf.length()/registro.length();
		int numeroCubetas = (int)cubetas.getrafLength()/cubetas.cubetaSize();
		int posAInsertar = 0;
		for(int i = 0; i<numeroRegistros; i++) {
			raf.seek(i*(registro.length()));
			RegIndice temporal = new RegIndice();
			temporal.read(raf);
			String regindice = temporal.getClave().trim();
			if(clave.endsWith(regindice)) {
				posAInsertar = temporal.getLiga();
				i = numeroRegistros;
			}
		}
		cubetas = cubetas.leerCubeta(posAInsertar);
		boolean hayEspacio = false;
		for(int i = 0; i<cubetas.registros.length; i++) {
			RegCubeta bus = cubetas.registros[i];
			System.out.println("[TABLA - insertarEntrada] Estado en i: " + i + " " + bus.getEstado());
			if(bus.getEstado()==0){
				System.out.println("[TABLA - insertarEntrada] Se encontró un espacio vacío en la posición: " + i);
				hayEspacio = true;
				bus.setEstado((byte)1);
				bus.setCodigo(clave);
				bus.setLiga(pos);
				cubetas.registros[i] = bus;
				i = cubetas.registros.length;
				cubetas.escribirCubeta(0);
			} //end if
		} //end for
		if(!hayEspacio) {
			System.out.println("[TABLA - insertarEntrada] Tamaño antes de split " + cubetas.getrafLength());
			cubetas.split(0);
			System.out.println("[TABLA - insertarEntrada] Tamaño después de split " + cubetas.getrafLength());
			cubetas.leerCubeta(0);
			if(cubetas.getGeneracion()>this.tamIndice) {
				duplicarTabla();
				acomodarPunteros();
			}
			insertarEntradaPost(archRegistro, pos);
		}
	}
	
	public void duplicarTabla() throws IOException{
		tamIndice++;
		int numeroDeRegistros = (int)Math.pow(2, tamIndice);
		for(int i = 0; i<numeroDeRegistros; i++) {
			String temporal = funcionHash(i);
			String recortada = temporal.substring(temporal.length()-tamIndice);
			raf.seek(i*(registro.length()));
			registro.setClave(recortada);
			registro.write(raf);
		}
	}
	
	public void acomodarPunteros() throws IOException{
		int numeroRegistros = (int)raf.length()/registro.length();
		int numeroCubetas = (int)cubetas.getrafLength()/cubetas.cubetaSize();
		for(int i = 0; i<numeroRegistros; i++) {
			raf.seek(i*(registro.length()));
			RegIndice temporal = new RegIndice();
			temporal.read(raf);
			String regindice = temporal.getClave().trim();
			for(int j = 0; j<numeroCubetas; j++) {
				cubetas.leerCubeta(j*(cubetas.cubetaSize()));
				String regcubeta = cubetas.getClavedeTabla().trim();
				if(regindice.endsWith(regcubeta)) {
					temporal.setLiga(j*(cubetas.cubetaSize()));
					j = numeroCubetas;
					raf.seek(i*(registro.length()));
					temporal.write(raf);
				}
			}
		}
	}
	
	public void mostrar() throws IOException{
		int numeroRegistros = (int)raf.length()/registro.length();
		for(int i = 0; i<numeroRegistros; i++) {
			raf.seek(i*(registro.length()));
			RegIndice temporal = new RegIndice();
			temporal.read(raf);
			System.out.println("[CLAVE] " + temporal.getClave()+ " [LIGA] " 
								+ temporal.getLiga());
		}
		System.out.println("-----CUBETAS-----");
		int numeroCubetas = (int)cubetas.getrafLength()/cubetas.cubetaSize();
		for(int i = 0; i<numeroCubetas; i++) {
			cubetas = cubetas.leerCubeta(i*(cubetas.cubetaSize()));
			System.out.println("[GENERACION] " + cubetas.getGeneracion() 
								+ " [CLAVE] " + cubetas.getClavedeTabla());
			for(int j = 0; j<Cubeta.CUBETAM;j++) {
				RegCubeta tempo = new RegCubeta();
				tempo = cubetas.registros[i];
				System.out.println("   [ESTADO] " + tempo.getEstado() 
									+ " [CODIGO] " + tempo.getCodigo() 
									+ " [APUNTADOR] " + tempo.getLiga());
			}
		}
	}
}
