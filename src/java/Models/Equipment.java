package Models;

public class Equipment {

    private String equipment_id;
    private String name;
    private String equipment_type;
    private String image_url;
    private float rental_price;
    private float damage_fee;
    private String status;
    private String description;

    public Equipment() {}

    public Equipment(String equipment_id, String name, String equipment_type,
                     String image_url, float rental_price, float damage_fee,
                     String status, String description) {
        this.equipment_id = equipment_id;
        this.name = name;
        this.equipment_type = equipment_type;
        this.image_url = image_url;
        this.rental_price = rental_price;
        this.damage_fee = damage_fee;
        this.status = status;
        this.description = description;
    }

    // ===== QUAN TRỌNG CHO JSP =====
    public String getId() {
        return equipment_id;
    }

    public void setId(String id) {
        this.equipment_id = id;
    }

    // ===== GETTER / SETTER CHUẨN =====
    public String getEquipment_id() {
        return equipment_id;
    }

    public void setEquipment_id(String equipment_id) {
        this.equipment_id = equipment_id;
    }

    public String getName() {
        return name;
    }

    public String getEquipmentType() {
        return equipment_type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEquipment_type(String equipment_type) {
        this.equipment_type = equipment_type;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setRental_price(float rental_price) {
        this.rental_price = rental_price;
    }

    public void setDamage_fee(float damage_fee) {
        this.damage_fee = damage_fee;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return image_url;
    }

    public float getRentalPrice() {
        return rental_price;
    }

    public float getDamageFee() {
        return damage_fee;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
}
