# GROUP1_BSIT_2B_PROJECT — Subscription Delivery & Container Asset Tracking System

## 1. Project Overview & Context

A Java console application for managing a water-container subscription business (BSIT 2B group project).

What it does:

- Registers customers and manages their subscription plans (e.g. Basic 5-Gal, Premium 5-Gal).
- Schedules and tracks deliveries, assigns drivers, and monitors container assets (`CNT-0001`, …) through their lifecycle.
- Records payments and produces operational reports.
- Provides role-based access: **Admin** (full main menu), **Customer** (customer portal), and **Delivery Personnel** (driver portal).

Entry point: `Main.java` creates an `AppContext` (in-memory data store with seed data) and launches `ConsoleUI`.

Seeded demo accounts (see `src/service/AppContext.java`):

- Admin — username `admin` / password `admin123`
- Delivery personnel — username `delivery` / password `delivery123`
- Customers self-register via the Sign-up option (customer accounts).

## 2. Environment & Prerequisites

- IntelliJ IDEA (the project ships with `GROUP1_BSIT_2B_PROJECT.iml`, `src/` marked as source root).
- An openjdk-26 (2) Oracle OpenJDK 26.0.2 in IntelliJ. No Maven/Gradle dependencies — nothing else to install.

## 3. Setup & Installation Guide

1. If you don't have the code yet, get it with git:
   - Open File Explorer and go to the folder where you want to keep the project.
   - Right-click an empty space in that folder and choose **Git Bash Here** (requires Git for Windows to be installed).
   - Clone the repository, replacing the URL with your repo URL:
     ```bash
     git clone <repository-url>
     ```
   - This creates a project folder with all the files inside.
2. Open the project folder in IntelliJ IDEA (`File > Open`, select the project root).
3. Open `src/Main.java` and click the green **Run** button (or `Run > Run 'Main'`).
4. Use the console in IntelliJ's Run window: log in with a seeded account (`admin` / `admin123`) or choose Sign-up to create a customer account.

Note: `out/` holds compiled `.class` files and is ignored by git.

## 4. Usage & Application Flow

Welcome menu (every launch):

1. **Log in** — 3 password attempts, then back to the welcome menu; `0` cancels.
2. **Sign up** — creates a CUSTOMER `User` plus a linked `Customer` record (full name, address, contact, email).
0. **Exit**.

After login, users are routed by role (see `src/ui/ConsoleUI.java`):

- **Admin** → Main Menu: Customer, Subscription, Delivery, Container, Payment, Driver, Reports, and User Management (admin-only).
- **Customer** → `CustomerPortal`: view own subscriptions, deliveries, and payments.
- **Delivery Personnel** → `DriverPortal`: view assigned deliveries and update delivery status.

Typical admin flow: register customer → create subscription on a plan → schedule delivery with a driver and containers → record payment → check Reports. Data is in-memory only and resets on restart.

## 5. Project Architecture & Folder Structure

No frameworks; layered plain-Java design. `AppContext` is the shared in-memory store (lists + ID sequences + `currentUser`); `service/*` classes hold business logic; `ui/*` classes handle console menus; `model/*` are domain entities; `util/*` are input/table helpers.

```text
.
├── src/
│   ├── Main.java              # Entry point: builds AppContext, starts ConsoleUI
│   ├── model/                 # Container, ContainerCondition, ContainerStatus,
│   │                          # Customer, Delivery, DeliveryStatus, Driver,
│   │                          # Payment, Role, Subscription, SubscriptionPlan,
│   │                          # SubscriptionStatus, User
│   ├── service/               # AppContext (store + seed data), AuthService,
│   │                          # ContainerService, CustomerService, DeliveryService,
│   │                          # DriverService, PaymentService, ReportService,
│   │                          # SubscriptionService, UserService
│   ├── ui/                    # ConsoleUI (login/signup + role routing + admin menu),
│   │                          # CustomerMenu, SubscriptionMenu, DeliveryMenu,
│   │                          # ContainerMenu, PaymentMenu, DriverMenu,
│   │                          # ReportMenu, UserMenu, CustomerPortal, DriverPortal
│   └── util/                  # InputHelper, TablePrinter
├── out/                       # Compiled classes (git-ignored)
├── GROUP1_BSIT_2B_PROJECT.iml # IntelliJ module config (src/ = source root)
└── README.md
```


