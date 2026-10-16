import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ParcelManager manager = ParcelManager.getInstance();

    public static void main(String[] args) {
        int choice;

        // Welcome message
        System.out.println("============================================");
        System.out.println("      WELCOME TO PARCEL DELIVERY SYSTEM     ");
        System.out.println("============================================");
        System.out.println("Manage your parcels easily and efficiently!");

        do {
            System.out.println("\n======= PARCEL DELIVERY SYSTEM =======");
            System.out.println("1. Add Parcel");
            System.out.println("2. View All Parcels");
            System.out.println("3. Change Delivery Service");
            System.out.println("4. Mark as Delivered");
            System.out.println("5. Exit");
            System.out.print("Choose: ");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                choice = 0;
            }

            switch (choice) {
                case 1:
                    addParcel();
                    break;
                case 2:
                    viewAllParcels();
                    break;
                case 3:
                    changeDeliveryService();
                    break;
                case 4:
                    markAsDelivered();
                    break;
                case 5:
                    System.out.println("Thank you for using our Parcel Delivery System!");
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice! Please pick 1-5.");
            }

        } while (choice != 5);
    }

    // ---------- 1. Add Parcel ----------
    private static void addParcel() {
        System.out.println("\n=========== ADD PARCEL ===========");

        String type = readParcelType();
        String tracking = readTrackingNumber();
        String receiver = readReceiverName();

        double maxWeight = type.equals("1") ? Document.MAX_WEIGHT
                : type.equals("2") ? Box.MAX_WEIGHT : Fragile.MAX_WEIGHT;

        double weight = readWeight(maxWeight);

        try {
            Parcel parcel;

            if (type.equals("1")) {
                int pages = readPages();
                parcel = new Document(tracking, receiver, weight, pages);
            } else {
                int length = readDimension("Length");
                int width = readDimension("Width");
                int height = readDimension("Height");

                if (type.equals("2")) {
                    parcel = new Box(tracking, receiver, weight,
                            length, width, height);
                } else {
                    String note = readHandlingNote();
                    parcel = new Fragile(tracking, receiver, weight,
                            length, width, height, note);
                }
            }

            // Keep asking until a valid delivery service is chosen
            while (true) {
                DeliveryService service = selectService();

                if (service == null) {
                    System.out.println("Invalid choice! Please pick 1-3.");
                } else if (!service.isAllowed(parcel)) {
                    System.out.println("Same Day delivery is only for parcels up to 10 kg!");
                } else {
                    parcel.setService(service);
                    break;
                }
            }

            manager.addParcel(parcel);

            System.out.println("Parcel added successfully!");
            System.out.printf("Delivery Fee: Php %.2f%n",
                    parcel.getDeliveryFee());

        } catch (InvalidInputException e) {
            System.out.println(e.getMessage());
        } catch (DuplicateParcelException e) {
            System.out.println(e.getMessage());
        }
    }

    // ---------- 2. View All Parcels ----------
    private static void viewAllParcels() {
        System.out.println("\n======================================= VIEW ALL PARCELS =======================================");

        if (manager.getParcels().isEmpty()) {
            System.out.println("No parcels yet.");
            return;
        }

        String format = "%-10s %-10s %-20s %-12s %-10s %-16s %-10s%n";

        System.out.printf(format, "Tracking", "Type", "Receiver",
                "Charge Wt", "Service", "Delivery Fee", "Status");

        for (int i = 0; i < 94; i++) {
            System.out.print("-");
        }
        System.out.println();

        for (Parcel p : manager.getParcels()) {
            String receiver = p.getReceiverName();

            if (receiver.length() > 20) {
                receiver = receiver.substring(0, 17) + "...";
            }

            System.out.printf(format,
                    p.getTrackingNumber(),
                    p.getType(),
                    receiver,
                    String.format("%.2f kg", p.getChargeableWeight()),
                    p.getService().getName(),
                    String.format("Php %.2f", p.getDeliveryFee()),
                    p.getStatus());
        }
    }

    // ---------- 3. Change Delivery Service ----------
    private static void changeDeliveryService() {
        System.out.println("\n=========== CHANGE DELIVERY SERVICE ===========");
        System.out.print("Enter Tracking Number: ");

        String tracking = scanner.nextLine().trim();

        try {
            Parcel parcel = manager.findParcel(tracking);

            if (parcel.getStatus() == Status.DELIVERED) {
                System.out.println("Parcel already delivered!");
                return;
            }

            DeliveryService service = selectService();

            if (service == null) {
                System.out.println("Invalid choice! The parcel keeps its old service.");
            } else if (!service.isAllowed(parcel)) {
                System.out.println("Same Day delivery is only for parcels up to 10 kg!");
            } else {
                parcel.setService(service);

                System.out.printf("Delivery service changed! New fee: Php %.2f%n",
                        parcel.getDeliveryFee());
            }

        } catch (ParcelNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    // ---------- 4. Mark as Delivered ----------
    private static void markAsDelivered() {
        System.out.println("\n=========== MARK AS DELIVERED ===========");
        System.out.print("Enter Tracking Number: ");

        String tracking = scanner.nextLine().trim();

        try {
            Parcel parcel = manager.findParcel(tracking);

            if (parcel.getStatus() == Status.DELIVERED) {
                System.out.println("Parcel was already delivered.");
            } else {
                parcel.markDelivered();
                System.out.println("Parcel delivered successfully!");
            }

        } catch (ParcelNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    // ---------- Input Helpers ----------

    private static String readParcelType() {
        while (true) {
            System.out.println("\nParcel Type");
            System.out.println("1. Document");
            System.out.println("2. Box");
            System.out.println("3. Fragile");
            System.out.print("Select type: ");

            String type = scanner.nextLine().trim();

            if (type.equals("1") || type.equals("2") || type.equals("3")) {
                return type;
            }

            System.out.println("Invalid parcel type! Please pick 1-3.");
        }
    }

    private static String readTrackingNumber() {
        while (true) {
            System.out.print("Tracking Number: ");
            String tracking = scanner.nextLine().trim();

            try {
                Parcel.validateTrackingNumber(tracking);
                manager.checkDuplicate(tracking);
                return tracking;

            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            } catch (DuplicateParcelException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static String readReceiverName() {
        while (true) {
            System.out.print("Receiver Name: ");
            String name = scanner.nextLine();

            try {
                Parcel.validateReceiverName(name);
                return name;

            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static double readWeight(double maxWeight) {
        while (true) {
            System.out.print("Weight (kg): ");
            String text = scanner.nextLine().trim();

            if (!isValidNumber(text)) {
                System.out.println("Please enter a valid number!");
                continue;
            }

            try {
                double weight = Double.parseDouble(text);
                Parcel.validateWeight(weight, maxWeight);
                return weight;

            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Validates a decimal number with an optional minus sign
    private static boolean isValidNumber(String text) {
        return text.matches("-?(?:[0-9]+\\.?[0-9]*|\\.[0-9]+)");
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);

            try {
                return Integer.parseInt(scanner.nextLine().trim());

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number!");
            }
        }
    }

    private static int readPages() {
        while (true) {
            int pages = readInt("Number of Pages (1-100): ");

            try {
                Document.validatePages(pages);
                return pages;

            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static int readDimension(String label) {
        while (true) {
            int value = readInt(label + " (cm): ");

            try {
                Box.validateDimension(value);
                return value;

            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static String readHandlingNote() {
        while (true) {
            System.out.print("Handling Note: ");
            String note = scanner.nextLine();

            try {
                Fragile.validateHandlingNote(note);
                return note;

            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static DeliveryService selectService() {
        System.out.println("\nDelivery services");
        System.out.println("1. Standard");
        System.out.println("2. Express");
        System.out.println("3. Same Day");
        System.out.print("Select service: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                return new StandardService();
            case "2":
                return new ExpressService();
            case "3":
                return new SameDayService();
            default:
                return null;
        }
    }
}