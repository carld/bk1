curl -X POST -H "Content-Type:application/json" -d '
{
  "query":"($id: Int!){account(id: $id){id name }}",
  "variables": {"id": 1}
} ' http://localhost:3002/graphql
echo
#   "variables": {"id": "1"}
#curl -i -XPOST -H 'Content-Type:application/graphql' -d '{ test }' http://localhost:3002/graphql
