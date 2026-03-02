package com.gtech.algashop.application.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageFilter {
    private int size = 15;
    private int page = 0;
}
