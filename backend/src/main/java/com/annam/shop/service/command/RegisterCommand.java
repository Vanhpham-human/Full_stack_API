package com.annam.shop.service.command;

public record RegisterCommand(String fullName, String email, String phone, String password, String address) {
}
