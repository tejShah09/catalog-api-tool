
package com.sigma.catalog.api.talendService;
public class test {

    public static void main(String[] args) {

        String[] ea = new String[] { 

            "Intent","Sales_Channel_To_Offer_Id","Sales_Channel_To_Offer_Attribute_Class","Sales_Channel_To_Offer_Attribute_Guid","Sales_Channel","Class_Type","Offer_Entity_Reference","Partner_Portal_Sales_Partner","Do_Not_Sell","Start_Date","End_Date","offerID"








    };
        System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        System.out.println("<schema dbmsId=\"postgres_id\">");
        for (String e : ea) {
            System.out.println("<column comment=\"\" default=\"\" key=\"false\" label=\"" + e
                    + "\" length=\"-1\" nullable=\"true\" originalDbColumnName=\"" + e
                    + "\" pattern=\"\" precision=\"0\" talendType=\"id_String\"  type=\"VARCHAR\"/>");
        }
        System.out.println("</schema>");

    }

}









