APP=bookshop

NS=antonio-dimarino-dev

.PHONY: ocp-login ns create-db kafka

ocp-login:
	@oc whoami || oc login $(OCP_API) --token=$(OCP_TOKEN)

ns:
	-oc new-project $(NS) || true

create-db:
	oc apply -f platform/addons/postgres.yaml

kafka:
	oc apply -f platform/addons/kafka.yaml