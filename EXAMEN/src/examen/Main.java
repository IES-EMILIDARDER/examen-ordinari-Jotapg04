package EXAMEN;

import examen.utils.GestorExamen;

public class Main {
    public static void main(String[] args) {
        GestorExamen gestor = new GestorExamen();

        try {
            gestor.carregaDades();

            // CORRECCIO: a?adido para ver el efecto de modificar los departamentos
            gestor.getDepartments().forEach(d->  d.setName(d.getName().toUpperCase() )  );
            
            gestor.mostraDepartments();
            gestor.mostraEmployees();
            gestor.mostraDepartmentsXEmployees();
            
            gestor.desaDepartmentsXEmpleatsCSV("c:\\temp\\departmentXEmpleats.cvs");

            
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

