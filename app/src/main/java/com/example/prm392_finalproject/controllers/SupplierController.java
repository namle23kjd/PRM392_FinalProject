package com.example.prm392_finalproject.controllers;

import java.util.List;

import com.example.prm392_finalproject.dao.SupplierDAO;
import com.example.prm392_finalproject.models.SupplierRequest;
import com.example.prm392_finalproject.models.SupplierResponse;
    
public class SupplierController {
    private SupplierDAO supplierDAO;

    public SupplierController() {
        this.supplierDAO = new SupplierDAO();
    }

    public List<SupplierResponse> getAllSuppliers() {
        return supplierDAO.getAllSuppliers();
    }

    public SupplierResponse getSupplierById(int supplier_id) {
        return supplierDAO.getSupplierById(supplier_id);
    }

    public void addSupplier(SupplierRequest supplier) {
        supplierDAO.addSupplier(supplier);
    }

    public void updateSupplier(SupplierRequest supplier, int supplier_id) {
        supplierDAO.updateSupplier(supplier, supplier_id);
    }

    public void deleteSupplier(int supplier_id) {
        supplierDAO.deleteSupplier(supplier_id);
    }
}