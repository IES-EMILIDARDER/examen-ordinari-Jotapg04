package examen.utils;

import EXAMEN.model.*;
import java.io.BufferedWriter;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;


public class GestorExamen {
    private Set<Department> departments = new HashSet<>();
    private Map<String, Employee> employees = new HashMap<>();
    private Map<Department, List<Employee>> departmentsXemployees = new HashMap<>();

    // CORRECCIO: a?adido para ver el efecto de modificar los departamentos
    public void setDepartments(Set<Department> departments) {
        this.departments = departments;
    }

    public Set<Department> getDepartments() {
        return departments;
    }
 
    
    final String MYSQL_CON = "c:\\temp\\mysql.con";
    GestorBBDD gestorBBDD = new GestorBBDD(MYSQL_CON);

    public void carregaDades()  throws SQLException, IOException {
        /* 1. Carregar les estructures:
            'departments'
            'empleats'
            (1,50 punts) Map<Department, List<Employee>> departmentsXempleats;
         */
         String sql =
                     """
                     SELECT email, first_name, last_name, e.department_id, department_name
                     FROM departments d, employees e
                     WHERE d.department_id = e.department_id
                     """;

         try (Connection conn = gestorBBDD.getConnectionFromFile();
             ResultSet res = gestorBBDD.executaQuerySQL(conn, sql)) {

            while (res.next()) {
                int depId = res.getInt("department_id");
                String depName = res.getString("department_name");
                Department dep = new Department(depId, depName);  // CORRECCIÓ: siempre creas el departamento
                departments.add(dep);                             // he modificado el 'main' para que veas el efecto de referenciar o no los objectos
                                                                  // si te fijas, el departamento en mayusculas no esta presente en muchos empleados
                String email = res.getString("email");
                String first = res.getString("first_name");
                String last = res.getString("last_name");

                Employee e = new Employee(first, last, email, dep);
                employees.put(email, e);

                departmentsXemployees.computeIfAbsent(dep, k -> new ArrayList<>()).add(e); //solo si no existe
            }
        }catch (SQLException e) {
            System.err.println("Error cargant BBDD: " + e.getMessage());
        }

    }
    
    public void mostraDepartments() {
        /*
           2. Mostra 'departments'
        */
        System.out.println("DEPARTMENTS");
        departments.stream()
                     // CORRECCIÓ: 'sorted(Comparator)' es demasiado complejo
                     // esperaba un 'sorted()' implementadando 'Comparator' en la clase ...
                    .sorted(Comparator.comparingInt(Department::getDepartmentId))
                    .forEach(System.out::println);
        System.out.println("////////////////////////////////////////");
    }   

    public void mostraEmployees() {
        /*
           2. Mostra 'employees'
        */
        System.out.println("EMPLOYEES");
        employees.values()
                 .stream()
                 // CORRECCIÓ: 'sorted(Comparator)' es demasiado complejo
                 // esperaba un 'sorted()' implementadando 'Comparator' en la clase ...
                 .sorted(Comparator.comparing(e -> e.getFirstName() + e.getLastName())) //tanto el nombre como el apellido
                 .forEach(System.out::println); //imprimimos
         System.out.println("////////////////////////////////////////"); //para que al separar sea más visual
    } 
    
    public void mostraDepartmentsXEmployees() {
        /*
           2. Mostra 'departmentsXemployees'
        */
        System.out.println("DEPARTMENTS X EMPLOYEES");
        departmentsXemployees.entrySet()
                             .stream()
                             .sorted(Map.Entry.comparingByKey(Comparator.comparingInt(Department::getDepartmentId)))
                             .forEach(entry -> {
                                Department d = entry.getKey();
                                List<Employee> empList = entry.getValue()
                                                              .stream()
                                                              // CORRECCIÓ: 'sorted(Comparator)' es demasiado complejo
                                                              // esperaba un 'sorted()' implementadando 'Comparator' en la clase ...
                                                              .sorted(Comparator.comparing(e -> e.getFirstName() + e.getLastName())) //tanto el nombre como el apellido
                                                              .toList(); //lo pasamos a lista
                                
                                    System.out.println(d + " - " + empList); //ejemplo: [DATOS DEL DEPARTAMENTO] -[DATOS DEL EMPLEADO]
                                });
                             
         System.out.println("////////////////////////////////////////");
    } 
    
    public void desaDepartmentsXEmpleatsCSV(String path) {
        /*
           3. Emmagatzema 'departmentsXemployees' en un arxiu .csv amb el següent format:
              #departmentId, name, email1;email2;email3;...;
        */
       try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(path))) {
            bw.write("#departmentId, name, email1;email2;email3;...;");
            departmentsXemployees.entrySet()
                                 .stream()
                                 .sorted(Map.Entry.comparingByKey(Comparator.comparingInt(Department::getDepartmentId))) //primero el departamento
                                 .forEach((Map.Entry<Department, List<Employee>> entry) -> {
                try {
                    Department d = entry.getKey();
                    List<String> emailsSorted = entry.getValue()
                                                     .stream()
                                                     .sorted(Comparator.comparing(e -> e.getFirstName() + e.getLastName())) // y después tanto el nombre como el apellido
                                                     .map(Employee::getEmail)
                                                     .toList();  
                    
                    // CORRECCIÓ: esta construcción está exageradamente bien construida ...
                    bw.write(d.getDepartmentId() + "," + d.getName() + "," + String.join(";",emailsSorted)); //con el string join separará por ;
                    bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                });
            
        }catch (IOException | NumberFormatException e) {
            System.err.println("Error desant DEPARTMENTS X EMPLOYEES CSV: " + e.getMessage());
        }

        
    }

}
