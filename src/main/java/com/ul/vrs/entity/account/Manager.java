package com.ul.vrs.entity.account;

import java.util.Optional;

import com.ul.vrs.entity.vehicle.Vehicle;
import com.ul.vrs.entity.vehicle.VehicleState;
import com.ul.vrs.service.DamageCheckingService;
import com.ul.vrs.service.SalesReportService;
import com.ul.vrs.service.VehicleManagerService;

public class Manager extends Account {

    public Manager(String name, String id, String password) {
        super(name, id, password);
    }

    /**
     * Add a new vehicle to the inventory
     * 
     * @param service The vehicle management service
     * @param vehicle The new vehicle to add
     */
    public void addVehicle(VehicleManagerService service, Vehicle vehicle) {
        service.addVehicle(vehicle);
        System.out.println("Added new vehicle: " + vehicle.getName() + " (ID: " + vehicle.getID() + ")");
    }
    

    /**
     * Modify or update details of an existing vehicle
     * 
     * @param service The vehicle management service
     * @param vehicle The vehicle with updated details
     */
    public void modifyVehicle(VehicleManagerService service, Vehicle updatedVehicle) {
        Optional<Vehicle> vehicleToModify = service.getVehicleById(updatedVehicle.getID());

        if (vehicleToModify.isPresent()) {
            Vehicle vehicle = vehicleToModify.get();

            // Update the ID (if needed)
            if (vehicle.getID() != updatedVehicle.getID()) {
                vehicle.setID(updatedVehicle.getID());
                System.out.println("Updated vehicle ID to: " + updatedVehicle.getID());
            } else {
                System.out.println("No changes to vehicle ID.");
            }

        } else {
            System.out.println("Vehicle with ID " + updatedVehicle.getID() + " not found.");
        }
    }



    /**
     * Remove a vehicle from the inventory
     * 
     * @param service The vehicle management service
     * @param vehicleId The ID of the vehicle to remove
     */
    public void removeVehicle(VehicleManagerService service, Vehicle vehicle) {
        service.deleteVehicle(vehicle.getID());
        System.out.println("Removed vehicle with ID: " + vehicle.getID());
    }
    
    /**
     * Assign a mechanic to a vehicle
     * 
     * @param service The vehicle management service
     * @param vehicle The vehicle to assign a mechanic to
     */
    public void assignMechanicToVehicle(VehicleManagerService service, Vehicle vehicle) {
        Optional<Vehicle> optionalVehicle = service.getVehicleById(vehicle.getID());
        if (optionalVehicle.isPresent()) {
            Vehicle foundVehicle = optionalVehicle.get();

            // Update the vehicle state to IN_MAINTENANCE
            foundVehicle.updateState(VehicleState.IN_MAINTENANCE);

            // Save the updated vehicle state using the service
            service.updateVehicle(foundVehicle.getID(), foundVehicle);

            // Log success message
            System.out.println("Assigned mechanic to vehicle: " + foundVehicle.getName() + " (ID: " + foundVehicle.getID() + ")");
        } else {
            // Log if the vehicle was not found
            System.out.println("Vehicle not found for ID: " + vehicle.getID());
        }
    }


    /**
     * Review customer feedback
     */
    public void reviewFeedback() {
        System.out.println("Reviewing customer feedback...");
        // Include logic to fetch and review feedback from a service or database
    }

    /**
     * Generate and view the sales report for all vehicles.
     * @return 
     *
     * @return Sales report as a string.
     */
    public String generateVehicleSalesReport() {
        System.out.println("Generating vehicle sales report...");
        // Include logic to generate and fetch the report from a service
        String report = SalesReportService.generateReport();
        System.out.println(report);
        return report;
    }

    /**
     * View damage assessment report for a vehicle
     * 
     * @param vehicle The vehicle for which to view the report
     */
    public String viewDamageAssessmentReport(Vehicle vehicle) {
        System.out.println("Viewing damage assessment report for vehicle: " + vehicle.getName() + " (ID: " + vehicle.getID() + ")");
        // Include logic to fetch and display the damage assessment report
        String report = DamageCheckingService.generateReport();
        System.out.println(report);
        return report;
    }
}