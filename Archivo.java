import java.io.*;

public class Archivo {
    
    private final int SIN_ASIGNAR = IndiceDenso.SIN_ASIGNAR;
    
	private RandomAccessFile raf = null;
	private IndiceDenso indiceDenso = null;
    
    /*-----------------------------------------------------------------
    / constructor: �ndice denso con una clave de b�squeda de 20 bytes
    /-----------------------------------------------------------------*/
    
	public Archivo( RandomAccessFile archivo,
                    RandomAccessFile indice )
	{
		raf = archivo;
		indiceDenso = new IndiceDenso( indice, 20 );
	}
    
    /*-----------------------------------------------------------------
    / inserta un registro al archivo
    /-----------------------------------------------------------------*/
    
	public void insertar( Registro registro ) throws IOException {
        
		int posicionIndice = indiceDenso.getPosicion( registro.getSucursal() );
        
		if( posicionIndice == indiceDenso.size()-1 ) {
            
			int posicionArchivo = (int) raf.length() / registro.length();
			insertarEn( posicionArchivo, registro );
            
            if( indiceDenso.getLiga( posicionIndice ) == SIN_ASIGNAR )
				indiceDenso.updateLiga( posicionIndice, posicionArchivo );
            
            } else {
            
			int posicionArchivo = indiceDenso.getLiga( posicionIndice + 1 );
			insertarEn( posicionArchivo, registro );
            
			if( indiceDenso.getLiga( posicionIndice ) == SIN_ASIGNAR )
				indiceDenso.updateLiga( posicionIndice, posicionArchivo );
            
			for( posicionIndice ++;
                 posicionIndice < indiceDenso.size(); posicionIndice ++ )
            {
				posicionArchivo = indiceDenso.getLiga( posicionIndice ) + 1;
				indiceDenso.updateLiga( posicionIndice, posicionArchivo );
			}
		}
	}
    
    /*-----------------------------------------------------------------
    / borra un registro del archivo
    /-----------------------------------------------------------------*/
    
    public boolean borrar( String nomSuc ) throws Exception {
        
        int posicionIndice = indiceDenso.find( nomSuc ); //0,1,....numero de registros del indice
        
        if( posicionIndice == SIN_ASIGNAR ) { return false; }
        
        else {
            Registro registro = new Registro();
            int posicion = indiceDenso.getLiga( posicionIndice );// posicion del registro en el archivo principal
            
            raf.seek( posicion * registro.length() );
            registro.read( raf );
            registro.setFlag( true );
            registro.setSucursal( "@Eliminado!@" ); // se puede quitar
            
            raf.seek( posicion * registro.length() );// eliminado
            registro.write( raf );
            
            if( raf.getFilePointer() == raf.length() ) {
                                                // compacta el archivo
                indiceDenso.borrarEntrada( posicionIndice );
                
            } else {
                
                registro.read( raf );           // lee el siguiente registro
                
                if( registro.compareTo( nomSuc ) == 0 ) {
                                                // actualiza la liga
                    indiceDenso.updateLiga( posicionIndice, posicion + 1 );
                    
                } else {
                                                // compacta el archivo
                    indiceDenso.borrarEntrada( posicionIndice );
                }
            }
            
            return true;
        }
    }
    
    /*-----------------------------------------------------------------
    / desplaza registros para insertar un registro en el archivo
    /-----------------------------------------------------------------*/
    
	private void insertarEn( int posicion, Registro registro ) throws IOException {
        
		int n = (int) raf.length() / registro.length();
        
		for( int i = n-1; i >= posicion; i -- ) {
            
			Registro temp = new Registro();
            
			raf.seek( i * temp.length() );
			temp.read( raf );
            
			raf.seek( (i+1) * temp.length() );
			temp.write( raf );
		}
        
		raf.seek( posicion * registro.length() );
		registro.write( raf );
	}
    
    /*-----------------------------------------------------------------
    / presenta los registros tanto del archivo como de su �ndice
    /-----------------------------------------------------------------*/
    
    public void mostrar() throws Exception {
        
		Registro registro = new Registro();
		int size = (int) raf.length() / registro.length();
        
		indiceDenso.mostrar();
        
		System.out.println( "N�mero de registros: " + size );
		raf.seek( 0 );
        
		for( int i = 0; i < size; i ++ ) {
            
			registro.read( raf );
            
			System.out.println( "( " + registro.getSucursal() + ", "
                                     + registro.getNumero() + ", "
                                     + registro.getNombre() + ", "
                                     + registro.getSaldo() + " )" );
		}
	}
    
    /*------------------------------------------------------------------
    /Busqueda Lineal
    */
    public void busquedaLineal(String clave, Registro registro, int num ) throws IOException {
        
		int n = (int) raf.length() / registro.length();
                boolean encontrado = false;
                
                int posicion = indiceDenso.busquedaLineal(clave);
                
               

		if(posicion != -1  )
                {
                    
                    raf.seek(posicion*registro.length());
                    registro.read(raf);
                    
                    System.out.println("Sucursal del registro existe"/*+" "+posicion+" "+registro.compareTo(clave)*/);
                    int i = posicion;
                    while(i < n && ( registro.compareTo(clave)== 0 || registro.deleteFlag() ))
                    //for(int i = posicion; i < n && registro.compareTo(clave)== 0;  i++) 
                    {
			
                       // System.out.println(posicion+" "+i+" "+n+" "+registro.compareTo(clave)+" "+registro.getNumero()+" "+registro.getSucursal());
                        
			if(registro.getNumero() == num )
                        {
                            System.out.println("El registro se encuentra en la posicón "+ i +" del archivo");
                            encontrado = true;
			}
                        
                        i++;
                        if(i < n){
                            raf.seek(i* registro.length());
                            registro.read(raf);}
                    }
                }
                else
                {
                     System.out.println("La sucursal no existe");       
                }
                if(!encontrado)
                    System.out.println("No existe el registro con tal numero de cuenta");
		
    }
    
    
    /*-----------------------------------------------------------------
    / cierra el archivo de datos
    /-----------------------------------------------------------------*/
    
    public void cerrar() throws IOException {
        
        raf.close();
        indiceDenso.cerrar();
    }
}
