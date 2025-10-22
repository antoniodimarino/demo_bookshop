package it.example.bookshop.user.service.dto;

import java.util.*;

import it.example.bookshop.common.dto.AddressDTO;

public record UserUpsert(String email, String firstName, String lastName, String phone, String role, List<AddressDTO> addresses) {

}
