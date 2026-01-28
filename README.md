# Catalog-Project
simple catalog project for lab

## Data Base:
CSV File with each row being a new entry

Functions:
- read (returns List<Entry>)
- write (takes List<Entry>)

## Entry Class
Properties:
- id (int)
- name (String)
- desciption (String)

## Backend Public Functions
- addEntry (name, description)
- editEntry (id, new_name, new_description)
- getAllEntries (returns List<Entry>)
