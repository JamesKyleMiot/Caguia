# Caguioa Bank Diagrams

Open the standalone files below in draw.io / diagrams.net:

- [flowchart.drawio](flowchart.drawio)
- [erd.drawio](erd.drawio)
- [usecase.drawio](usecase.drawio)

## Pages
- Flowchart: end-to-end system flow from login to user and admin actions.
- ERD: the main database tables and relationships.
- Use Case: the main actors and actions in the banking system.

## Main Flow
- User/Admin log in.
- The system authenticates the session.
- Users can deposit, withdraw, request loans, view transactions, and read notifications.
- If a user already has an active loan, the system prompts them to pay first.
- Admins can manage overdue loans, send reminders, suspend/reactivate accounts, and initialize the database.
- Notifications are stored in `user_messages` and shown in the user dashboard.

## Tables Covered
- users
- admin
- loans
- transactions
- user_messages
- account_audit_log

## Notes
- Each diagram is now stored as its own `.drawio` file.
- The original combined file can still be kept as a reference if you want it.
