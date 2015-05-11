import java.io.*;

public class TabladeHash {
	
	private static int tamIndice = 0;
	public RegIndice registro = null;
    private RandomAccessFile raf = null;
    private Cubeta cubetas = null;
	
	public TabladeHash(RandomAccessFile indice, RandomAccessFile cubetas) {
		raf = indice;
		registro = new RegIndice();
		this.cubetas = new Cubeta(cubetas);
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
			cubetas.crearCubeta(0, 0, temp.getClave()); 
			cubetas = cubetas.leerCubeta(0);
			RegCubeta[] temporal = cubetas.getRegistros();
			RegCubeta nueva = new RegCubeta();
			nueva.setEstado((byte)1);
			nueva.setCodigo(clave);
			nueva.setLiga(pos);
			temporal[0] = nueva;
			for(int i = 0; i<temporal.length;i++) {
				RegCubeta tem = temporal[i];
			}
			cubetas.registros = temporal;
			cubetas.escribirCubeta(0);
		} else if (raf.length()==registro.length()) { //Sólo existe una entrada en el registro
													//Cualquier clave puede entrar a la cubeta asociada
			System.out.println("[TABLA - insert] Solo existe una entrada");
			cubetas = cubetas.leerCubeta(0);
			if(cubetas.getLastIndex()==cubetas.registros.length) { //La cubeta está llena
				System.out.println("[TABLA - insertPos] Cubeta llena");
				cubetas.split(0);
				cubetas = cubetas.leerCubeta(0);
				System.out.println("[TABLA - inserPost] Generacion de cubeta: "
						+ cubetas.getGeneracion() + "es mayor a tamIndice? "
						+ this.tamIndice);
				if(cubetas.getGeneracion()>this.tamIndice) {
					duplicarTabla();
					acomodarPunteros();
				} else {
					acomodarPunteros();
				}
				insertarEntradaPost(archRegistro, pos);
			} else {
				System.out.println("[TABLA - insert] lastIndex: " + cubetas.getLastIndex()
						+ " longitud de registros " + cubetas.registros.length);
				for(int i = cubetas.getLastIndex(); i<cubetas.registros.length; i++) {
					RegCubeta bus = cubetas.registros[i];
					System.out.println("[TABLA - insertPost] Insertando: " +  archRegistro.getNumero());
					System.out.println("[TABLA - insertPost] estado de i:" + i + " " + bus.getEstado());
					if(bus.getEstado()==0){
						bus.setEstado((byte) 1);
						bus.setCodigo(clave);
						bus.setLiga(pos);
						cubetas.registros[i] = bus;
						cubetas.setLastIndex(i+1);
						i = cubetas.registros.length;
						cubetas.escribirCubeta(0);
					} //end if
				} //end for
			}
		} else { //Ninguno de los casos anteriores
			insertarEntradaPost(archRegistro, pos);
		}
	} //end insertarEntrada
	
	public void insertarEntradaPost(Registro archRegistro, int pos) throws IOException {
		int n = (int) cubetas.getrafLength() / cubetas.cubetaSize();
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
		if(cubetas.getLastIndex()==cubetas.registros.length) { //La cubeta está llena
			System.out.println("[TABLA - insertPos] Cubeta llena");
			cubetas.split(posAInsertar);
			cubetas = cubetas.leerCubeta(posAInsertar);
			System.out.println("[TABLA - inserPost] Generacion de cubeta: "
					+ cubetas.getGeneracion() + "es mayor a tamIndice? "
					+ this.tamIndice);
			if(cubetas.getGeneracion()>this.tamIndice) {
				duplicarTabla();
				acomodarPunteros();
			} else {
				acomodarPunteros();
			}
			insertarEntradaPost(archRegistro, pos);
		} else {
			System.out.println("[TABLA - insert] lastIndex: " + cubetas.getLastIndex()
					+ " longitud de registros " + cubetas.registros.length);
			for(int i = cubetas.getLastIndex(); i<cubetas.registros.length; i++) {
				RegCubeta bus = cubetas.registros[i];
				System.out.println("[TABLA - insertPost] Insertando: " +  archRegistro.getNumero());
				System.out.println("[TABLA - insertPost] estado de i:" + i + " " + bus.getEstado());
				if(bus.getEstado()==0){
					bus.setEstado((byte) 1);
					bus.setCodigo(clave);
					bus.setLiga(pos);
					cubetas.registros[i] = bus;
					for(int x = i; x<cubetas.registros.length; x++) {
						RegCubeta temporal = cubetas.registros[x];
						if(temporal.getEstado()==1) {
							cubetas.setLastIndex(x+1);
							System.out.println("[TABLA - insertPost]");
						} else {
							cubetas.setLastIndex(x);
							x = cubetas.registros.length;
						}
					}
					i = cubetas.registros.length;
					cubetas.escribirCubeta(posAInsertar);
				} //end if
			} //end for
		}
	}
	
	public void duplicarTabla() throws IOException{
		System.out.println("[TABLA - duplicar] Valor de tamIndice " + (tamIndice+1));
		TabladeHash.tamIndice = (TabladeHash.tamIndice+1);
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
				cubetas = cubetas.leerCubeta(j*(cubetas.cubetaSize()));
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
			RegCubeta[] tempo = cubetas.getRegistrosForzado(i*(cubetas.cubetaSize()));
			for(int j = 0; j<Cubeta.CUBETAM;j++) {
				RegCubeta temporal = tempo[j];
				System.out.println("   [ESTADO] " + temporal.getEstado() 
									+ " [CODIGO] " + temporal.getCodigo() 
									+ " [APUNTADOR] " + temporal.getLiga());
			}
		}
	}
}
