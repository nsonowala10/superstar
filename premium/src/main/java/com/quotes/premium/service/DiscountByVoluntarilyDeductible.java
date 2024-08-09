package com.quotes.premium.service;

import com.quotes.premium.dto.AmountDivision;
import com.quotes.premium.dto.Insured;
import com.quotes.premium.dto.VoluntarilyDeductible;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DiscountByVoluntarilyDeductible {

    static List<String> ALLOWED_VALUES = Arrays.asList("10000", "20000", "50000", "100000", "200000","300000","400000","500000","750000","1000000");

    static double[][] table=
            {
                    {1,1,1,1,1,1,1,1,1,1},
                    {1,1,1,1,1,1,1,1,1,1},
                    {1,1,1,1,1,1,1,1,1,1},
                    {1,1,1,1,1,1,1,1,1,1},
                    {1,1,1,1,1,1,1,1,1,1},
                    {1,1,1,1,1,1,1,1,1,1},
                    {1,1,1,1,1,1,1,1,1,1}
            };


    public static void calculateDiscount(AmountDivision amountDivision, VoluntarilyDeductible voluntarilyDeductible, List<Insured> insured) {
        if(voluntarilyDeductible.isDeductible() && ALLOWED_VALUES.contains(voluntarilyDeductible.getDeductibleAmount())){
            Optional<Integer> optional = insured.stream().filter(ins->ins.getType().equals("parent")).map(ins->ins.getAge()).max(Comparator.naturalOrder());
            int row_num = -1;
            int maxAge = optional.get();
            double loading = 0.0d;
            if(maxAge <= 35)
                row_num = 0;

            else if(maxAge <= 45)
                row_num = 1;

            else if(maxAge <= 50)
                row_num = 2;

            else if(maxAge <= 55)
                row_num = 3;

            else if(maxAge <= 60)
                row_num = 4;

            else if(maxAge <= 65)
                row_num = 5;

            else row_num = 6;

           int col_num = -1;
           for(int index = 0; index < ALLOWED_VALUES.size(); index ++){
               if(voluntarilyDeductible.getDeductibleAmount().equals(ALLOWED_VALUES.get(index))){
                   col_num = index;
                   break;
               }
           }

           amountDivision.setDeductibleDiscount(amountDivision.getBasePremium()*table[row_num][col_num]/100.0d);
           return ;
        }
    }
}
