-- public.users definition

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users (
                              id uuid NOT NULL DEFAULT gen_random_uuid(),
                              user_group uuid NULL,
                              "name" text NULL,
                              email text NOT NULL,
                              "password" text NOT NULL,
                              currency_default uuid NULL,
                              CONSTRAINT users_pk PRIMARY KEY (id)
);


-- public.users foreign keys

ALTER TABLE public.users ADD CONSTRAINT users_currencies_fk FOREIGN KEY (currency_default) REFERENCES public.currencies(id);

-- public.user_roles definition

-- Drop table

-- DROP TABLE public.user_roles;

CREATE TABLE public.user_roles (
                                   user_id uuid NOT NULL,
                                   "role" text NOT NULL,
                                   CONSTRAINT user_roles_unique UNIQUE (user_id, role)
);


-- public.user_roles foreign keys

ALTER TABLE public.user_roles ADD CONSTRAINT user_roles_users_fk FOREIGN KEY (user_id) REFERENCES public.users(id);

-- public.currencies definition

-- Drop table

-- DROP TABLE public.currencies;

CREATE TABLE public.currencies (
                                   id uuid NOT NULL DEFAULT gen_random_uuid(),
                                   user_group uuid NOT NULL,
                                   "name" text NULL,
                                   hidden bool NULL,
                                   CONSTRAINT currencies_pk PRIMARY KEY (id)
);


-- public.currencies foreign keys

ALTER TABLE public.currencies ADD CONSTRAINT currencies_users_fk FOREIGN KEY (user_group) REFERENCES public.users(id);

-- public.kinds definition

-- Drop table

-- DROP TABLE public.kinds;

CREATE TABLE public.kinds (
                              id uuid NOT NULL DEFAULT gen_random_uuid(),
                              user_group uuid NOT NULL,
                              "name" text NULL,
                              "type" text NOT NULL,
                              hidden bool NOT NULL,
                              CONSTRAINT kinds_pk PRIMARY KEY (id)
);


-- public.kinds foreign keys

ALTER TABLE public.kinds ADD CONSTRAINT kinds_users_fk FOREIGN KEY (user_group) REFERENCES public.users(id);

-- public.budget_items definition

-- Drop table

-- DROP TABLE public.budget_items;

CREATE TABLE public.budget_items (
                                     id uuid NOT NULL DEFAULT gen_random_uuid(),
                                     user_group uuid NOT NULL,
                                     user_id uuid NOT NULL,
                                     kind_id uuid NOT NULL,
                                     currency_id uuid NOT NULL,
                                     date_at date NOT NULL,
                                     created_at timestamp NOT NULL,
                                     description text NULL,
                                     price numeric NOT NULL,
                                     CONSTRAINT budget_items_pk PRIMARY KEY (id)
);


-- public.budget_items foreign keys

ALTER TABLE public.budget_items ADD CONSTRAINT budget_items_currencies_fk FOREIGN KEY (currency_id) REFERENCES public.currencies(id);
ALTER TABLE public.budget_items ADD CONSTRAINT budget_items_kinds_fk FOREIGN KEY (kind_id) REFERENCES public.kinds(id);
ALTER TABLE public.budget_items ADD CONSTRAINT budget_items_users_fk FOREIGN KEY (user_id) REFERENCES public.users(id);
ALTER TABLE public.budget_items ADD CONSTRAINT budget_items_users_fk_1 FOREIGN KEY (user_group) REFERENCES public.users(id);

-- public.regular_operations definition

-- Drop table

-- DROP TABLE public.regular_operations;

CREATE TABLE public.regular_operations (
                                           id uuid NOT NULL DEFAULT gen_random_uuid(),
                                           user_id uuid NOT NULL,
                                           user_group uuid NOT NULL,
                                           "every" text NOT NULL,
                                           day_of_month int4 NULL,
                                           kind_id uuid NOT NULL,
                                           description text NULL,
                                           price numeric NOT NULL,
                                           currency_id uuid NOT NULL,
                                           CONSTRAINT regular_operations_pk PRIMARY KEY (id)
);


-- public.regular_operations foreign keys

ALTER TABLE public.regular_operations ADD CONSTRAINT regular_operations_currencies_fk FOREIGN KEY (currency_id) REFERENCES public.currencies(id);
ALTER TABLE public.regular_operations ADD CONSTRAINT regular_operations_kinds_fk FOREIGN KEY (kind_id) REFERENCES public.kinds(id);
ALTER TABLE public.regular_operations ADD CONSTRAINT regular_operations_users_fk FOREIGN KEY (user_id) REFERENCES public.users(id);
ALTER TABLE public.regular_operations ADD CONSTRAINT regular_operations_users_fk_1 FOREIGN KEY (user_group) REFERENCES public.users(id);

-- public.join_requests definition

-- Drop table

-- DROP TABLE public.join_requests;

CREATE TABLE public.join_requests (
                                      id uuid NOT NULL DEFAULT gen_random_uuid(),
                                      user_group uuid NOT NULL,
                                      user_id uuid NOT NULL,
                                      created_at timestamp NOT NULL,
                                      accepted_at timestamp NULL,
                                      declined_at timestamp NULL,
                                      CONSTRAINT join_requests_pk PRIMARY KEY (id)
);


-- public.join_requests foreign keys

ALTER TABLE public.join_requests ADD CONSTRAINT join_requests_users_fk FOREIGN KEY (user_id) REFERENCES public.users(id);
ALTER TABLE public.join_requests ADD CONSTRAINT join_requests_users_fk_1 FOREIGN KEY (user_group) REFERENCES public.users(id);